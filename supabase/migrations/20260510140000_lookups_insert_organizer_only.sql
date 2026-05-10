-- =============================================================================
-- 20260510140000_lookups_insert_organizer_only.sql
-- -----------------------------------------------------------------------------
-- RBAC: only organizers/admins may add new public.areas / public.categories
-- rows. General "user" accounts can still SELECT them (read remains open).
--
-- Replaces the wide `authenticated`-only INSERT policy added by
-- 20260508100000_areas_categories_insert_authenticated.sql, which let any
-- signed-in user spam the lookup tables.
--
-- The mobile Create Event screen already auto-inserts a typed area name when
-- the user picks one that does not yet exist; this migration ensures only the
-- right role can do that. Failures from this policy surface to the client as
-- PostgREST 42501 (RLS) and are mapped to the "ask for organizer access"
-- string in CreateEventViewModel.humanizePublishError.
--
-- Role checks use public.current_user_can_insert_lookup_rows() (SECURITY
-- DEFINER) so nested RLS on public.profiles cannot block the role lookup.
-- =============================================================================

-- Shared helper (see also 20260510150000 for DBs that already ran an older
-- version of this migration); CREATE OR REPLACE keeps idempotency.
CREATE OR REPLACE FUNCTION public.current_user_can_insert_lookup_rows()
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

REVOKE ALL ON FUNCTION public.current_user_can_insert_lookup_rows() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.current_user_can_insert_lookup_rows() TO authenticated;
GRANT EXECUTE ON FUNCTION public.current_user_can_insert_lookup_rows() TO service_role;

-- ---------------------------------------------------------------------------
-- public.areas
-- ---------------------------------------------------------------------------
DROP POLICY IF EXISTS areas_insert_authenticated ON public.areas;
DROP POLICY IF EXISTS areas_insert_organizer ON public.areas;

CREATE POLICY areas_insert_organizer ON public.areas
  FOR INSERT TO authenticated
  WITH CHECK (
    auth.uid() IS NOT NULL
    AND public.current_user_can_insert_lookup_rows()
  );

-- The earlier migration granted INSERT to authenticated; keep the grant (RLS
-- still gates the row), but make sure the privilege exists so PostgREST
-- doesn't 401 before evaluating the policy.
GRANT INSERT ON TABLE public.areas TO authenticated;

-- ---------------------------------------------------------------------------
-- public.categories
-- ---------------------------------------------------------------------------
DROP POLICY IF EXISTS categories_insert_authenticated ON public.categories;
DROP POLICY IF EXISTS categories_insert_organizer ON public.categories;

CREATE POLICY categories_insert_organizer ON public.categories
  FOR INSERT TO authenticated
  WITH CHECK (
    auth.uid() IS NOT NULL
    AND public.current_user_can_insert_lookup_rows()
  );

GRANT INSERT ON TABLE public.categories TO authenticated;

COMMENT ON POLICY areas_insert_organizer ON public.areas IS
  'Only organizer/admin may add a new area row.';
COMMENT ON POLICY categories_insert_organizer ON public.categories IS
  'Only organizer/admin may add a new category row.';
