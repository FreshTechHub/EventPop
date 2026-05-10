-- =============================================================================
-- 20260510200000_eventpop_hosting_rbac_complete.sql
-- -----------------------------------------------------------------------------
-- EventPop — consolidated RBAC + hosting (idempotent).
--
-- Apply via: supabase db push   OR   paste into Supabase SQL Editor → Run.
--
-- What this fixes end-to-end:
--   • public.user_role enum + public.profiles.role
--   • get_my_role() RPC for the mobile client
--   • user_can_create_event() as SECURITY DEFINER reading profiles (avoids nested
--     RLS bugs where EXISTS (SELECT … FROM profiles …) inside another table’s
--     policy fails even for real organizers)
--   • get_host_event_quota() returning subscription + hosted count + role
--   • public.events INSERT/UPDATE/DELETE policies for hosts
--   • public.areas / public.categories INSERT policies for new free-form areas
--   • storage.objects policies for bucket `event-images` (organizer uploads)
--   • event-images bucket bootstrap
--
-- Hosting rule (matches app): only organizer/admin may create events; subscription
-- does not gate organizers (role-only check).
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Enum + profiles.role
-- ---------------------------------------------------------------------------
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_type t
    JOIN pg_namespace n ON n.oid = t.typnamespace
    WHERE t.typname = 'user_role' AND n.nspname = 'public'
  ) THEN
    CREATE TYPE public.user_role AS ENUM ('user', 'organizer', 'admin');
  END IF;
END $$;

ALTER TABLE public.profiles
  ADD COLUMN IF NOT EXISTS subscription_active boolean NOT NULL DEFAULT false;

ALTER TABLE public.profiles
  ADD COLUMN IF NOT EXISTS subscription_expires_at timestamptz;

ALTER TABLE public.profiles
  ADD COLUMN IF NOT EXISTS role public.user_role NOT NULL DEFAULT 'user';

CREATE INDEX IF NOT EXISTS idx_profiles_role ON public.profiles (role);

COMMENT ON COLUMN public.profiles.role IS
  'RBAC: user | organizer | admin. Only organizer/admin may host events.';

-- ---------------------------------------------------------------------------
-- 2. Client RPC: caller’s role
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.get_my_role()
RETURNS public.user_role
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT COALESCE(
    (SELECT p.role FROM public.profiles p WHERE p.id = auth.uid()),
    'user'::public.user_role
  );
$$;

COMMENT ON FUNCTION public.get_my_role() IS
  'Returns the caller''s RBAC role from public.profiles (defaults to user).';

REVOKE ALL ON FUNCTION public.get_my_role() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.get_my_role() TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_my_role() TO service_role;

-- ---------------------------------------------------------------------------
-- 3. Drop legacy lookup-insert policies before retiring the helper they used.
--    (Policies are recreated to use user_can_create_event() in section 7.)
-- ---------------------------------------------------------------------------
DROP POLICY IF EXISTS areas_insert_organizer ON public.areas;
DROP POLICY IF EXISTS areas_insert_authenticated ON public.areas;
DROP POLICY IF EXISTS categories_insert_organizer ON public.categories;
DROP POLICY IF EXISTS categories_insert_authenticated ON public.categories;

DROP FUNCTION IF EXISTS public.current_user_can_insert_lookup_rows();

-- ---------------------------------------------------------------------------
-- 4. Core gate: organizer/admin — SECURITY DEFINER so policies never lose the
--    profiles row to nested RLS. Used by events + areas + categories + storage.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.user_can_create_event()
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT EXISTS (
    SELECT 1
    FROM public.profiles p
    WHERE p.id = auth.uid()
      AND p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
  );
$$;

COMMENT ON FUNCTION public.user_can_create_event() IS
  'True when caller is organizer/admin (reads profiles as definer; used by RLS).';

REVOKE ALL ON FUNCTION public.user_can_create_event() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.user_can_create_event() TO authenticated;
GRANT EXECUTE ON FUNCTION public.user_can_create_event() TO service_role;

-- ---------------------------------------------------------------------------
-- 5. Host quota RPC (shape includes role — must DROP if return type changed)
-- ---------------------------------------------------------------------------
DROP FUNCTION IF EXISTS public.get_host_event_quota();

CREATE OR REPLACE FUNCTION public.get_host_event_quota()
RETURNS TABLE(
  subscription_active boolean,
  hosted_event_count bigint,
  role public.user_role
)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT
    COALESCE(
      (SELECT p.subscription_active FROM public.profiles p WHERE p.id = auth.uid()),
      false
    ) AS subscription_active,
    COALESCE(
      (SELECT COUNT(*)::bigint FROM public.events e WHERE e.created_by = auth.uid()),
      0::bigint
    ) AS hosted_event_count,
    COALESCE(
      (SELECT p.role FROM public.profiles p WHERE p.id = auth.uid()),
      'user'::public.user_role
    ) AS role;
$$;

COMMENT ON FUNCTION public.get_host_event_quota() IS
  'subscription_active, hosted_event_count, and role for the current user.';

REVOKE ALL ON FUNCTION public.get_host_event_quota() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.get_host_event_quota() TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_host_event_quota() TO service_role;

-- ---------------------------------------------------------------------------
-- 6. Events: created_by trigger + policies
-- ---------------------------------------------------------------------------
ALTER TABLE public.events
  ADD COLUMN IF NOT EXISTS created_by uuid REFERENCES auth.users (id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_events_created_by ON public.events (created_by);

CREATE OR REPLACE FUNCTION public.events_set_created_by()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  NEW.created_by := auth.uid();
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS events_set_created_by ON public.events;
CREATE TRIGGER events_set_created_by
  BEFORE INSERT ON public.events
  FOR EACH ROW
  EXECUTE FUNCTION public.events_set_created_by();

GRANT INSERT, UPDATE, DELETE ON TABLE public.events TO authenticated;

DROP POLICY IF EXISTS events_insert_tier ON public.events;
CREATE POLICY events_insert_tier ON public.events
  FOR INSERT TO authenticated
  WITH CHECK (
    created_by = auth.uid()
    AND auth.uid() IS NOT NULL
    AND public.user_can_create_event()
  );

DROP POLICY IF EXISTS events_update_own ON public.events;
CREATE POLICY events_update_own ON public.events
  FOR UPDATE TO authenticated
  USING (created_by = auth.uid())
  WITH CHECK (created_by = auth.uid());

DROP POLICY IF EXISTS events_delete_own ON public.events;
CREATE POLICY events_delete_own ON public.events
  FOR DELETE TO authenticated
  USING (created_by = auth.uid());

-- ---------------------------------------------------------------------------
-- 7. Lookup tables: organizers may INSERT new area / category rows
-- ---------------------------------------------------------------------------
GRANT INSERT ON TABLE public.areas TO authenticated;
GRANT INSERT ON TABLE public.categories TO authenticated;

DROP POLICY IF EXISTS areas_insert_authenticated ON public.areas;
DROP POLICY IF EXISTS areas_insert_organizer ON public.areas;
CREATE POLICY areas_insert_organizer ON public.areas
  FOR INSERT TO authenticated
  WITH CHECK (
    auth.uid() IS NOT NULL
    AND public.user_can_create_event()
  );

DROP POLICY IF EXISTS categories_insert_authenticated ON public.categories;
DROP POLICY IF EXISTS categories_insert_organizer ON public.categories;
CREATE POLICY categories_insert_organizer ON public.categories
  FOR INSERT TO authenticated
  WITH CHECK (
    auth.uid() IS NOT NULL
    AND public.user_can_create_event()
  );

COMMENT ON POLICY areas_insert_organizer ON public.areas IS
  'Organizer/admin may add a new area row (free-form area names).';
COMMENT ON POLICY categories_insert_organizer ON public.categories IS
  'Organizer/admin may add a category row if missing (matches app resolve).';

-- ---------------------------------------------------------------------------
-- 8. Storage: event-images bucket + RLS (uploads for hosts only)
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.bootstrap_event_images_bucket()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = storage, public
AS $$
BEGIN
  INSERT INTO storage.buckets (id, name, public)
  VALUES ('event-images', 'event-images', true)
  ON CONFLICT (id) DO NOTHING;
END;
$$;

DO $$
BEGIN
  PERFORM public.bootstrap_event_images_bucket();
END $$;

CREATE OR REPLACE FUNCTION public.trg_profiles_ensure_event_images_bucket()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  PERFORM public.bootstrap_event_images_bucket();
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS profiles_ensure_event_images_bucket ON public.profiles;
CREATE TRIGGER profiles_ensure_event_images_bucket
  AFTER INSERT ON public.profiles
  FOR EACH ROW
  EXECUTE FUNCTION public.trg_profiles_ensure_event_images_bucket();

DROP POLICY IF EXISTS event_images_objects_select_public ON storage.objects;
CREATE POLICY event_images_objects_select_public ON storage.objects
  FOR SELECT TO anon, authenticated
  USING (bucket_id = 'event-images');

DROP POLICY IF EXISTS event_images_objects_insert_own_prefix ON storage.objects;
CREATE POLICY event_images_objects_insert_own_prefix ON storage.objects
  FOR INSERT TO authenticated
  WITH CHECK (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
    AND public.user_can_create_event()
  );

DROP POLICY IF EXISTS event_images_objects_update_own_prefix ON storage.objects;
CREATE POLICY event_images_objects_update_own_prefix ON storage.objects
  FOR UPDATE TO authenticated
  USING (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
    AND public.user_can_create_event()
  )
  WITH CHECK (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
    AND public.user_can_create_event()
  );

DROP POLICY IF EXISTS event_images_objects_delete_own_prefix ON storage.objects;
CREATE POLICY event_images_objects_delete_own_prefix ON storage.objects
  FOR DELETE TO authenticated
  USING (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
    AND public.user_can_create_event()
  );

-- ---------------------------------------------------------------------------
-- 9. Sanity check (returns one row; both columns should be true)
-- ---------------------------------------------------------------------------
-- SELECT
--   EXISTS (
--     SELECT 1 FROM pg_proc p
--     JOIN pg_namespace n ON n.oid = p.pronamespace
--     WHERE n.nspname = 'public' AND p.proname = 'user_can_create_event' AND p.prosecdef
--   ) AS user_can_create_event_is_security_definer,
--   EXISTS (
--     SELECT 1 FROM pg_policy pol
--     JOIN pg_class c ON c.oid = pol.polrelid
--     JOIN pg_namespace ns ON ns.oid = c.relnamespace
--     WHERE ns.nspname = 'public' AND c.relname = 'areas' AND pol.polname = 'areas_insert_organizer'
--   ) AS areas_policy_present;
