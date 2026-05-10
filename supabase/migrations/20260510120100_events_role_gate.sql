
CREATE OR REPLACE FUNCTION public.user_can_create_event()
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY INVOKER
SET search_path = public
AS $$
  SELECT EXISTS (
    SELECT 1
    FROM public.profiles p
    WHERE p.id = auth.uid()
      AND p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
      AND (
        p.subscription_active
        OR (
          (SELECT COUNT(*)::bigint FROM public.events e WHERE e.created_by = auth.uid()) < 2
        )
      )
  );
$$;

COMMENT ON FUNCTION public.user_can_create_event() IS
  'True if caller is an organizer/admin and (subscribed OR under 2 hosted events).';

-- ---------------------------------------------------------------------------
-- Extend the host quota RPC to also return the caller's role, so the mobile
-- client can fetch role + quota in one round-trip when opening Create Event.
-- The previous version returned a different row type, so we must DROP first
-- (Postgres rejects CREATE OR REPLACE when the OUT-parameter shape changes).
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
  'Returns subscription_active, hosted_event_count, and role for the current user.';

REVOKE ALL ON FUNCTION public.get_host_event_quota() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.get_host_event_quota() TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_host_event_quota() TO service_role;

-- ---------------------------------------------------------------------------
-- Storage: tighten event-images uploads to organizer/admin only.
-- Read remains public (event posters need to be visible to everyone).
-- Owner-prefix path constraint is preserved so organizers cannot overwrite
-- each other's keys.
-- ---------------------------------------------------------------------------

DROP POLICY IF EXISTS event_images_objects_insert_own_prefix ON storage.objects;
CREATE POLICY event_images_objects_insert_own_prefix ON storage.objects
  FOR INSERT TO authenticated
  WITH CHECK (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
    AND EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid()
        AND p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
    )
  );

DROP POLICY IF EXISTS event_images_objects_update_own_prefix ON storage.objects;
CREATE POLICY event_images_objects_update_own_prefix ON storage.objects
  FOR UPDATE TO authenticated
  USING (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
    AND EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid()
        AND p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
    )
  )
  WITH CHECK (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
    AND EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid()
        AND p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
    )
  );

DROP POLICY IF EXISTS event_images_objects_delete_own_prefix ON storage.objects;
CREATE POLICY event_images_objects_delete_own_prefix ON storage.objects
  FOR DELETE TO authenticated
  USING (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
    AND EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid()
        AND p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
    )
  );
