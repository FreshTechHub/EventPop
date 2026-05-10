-- =============================================================================
-- 20260510150000_lookup_insert_rls_security_definer.sql
-- -----------------------------------------------------------------------------
-- Fix: INSERT policies on public.areas / public.categories used an EXISTS
-- subquery against public.profiles. With RLS enabled on profiles, nested
-- evaluation can fail to "see" the caller's row even though profiles_select_own
-- should allow it — depending on Postgres/Supabase versions and policy order.
--
-- Solution: evaluate host role in a STABLE SECURITY DEFINER function owned by
-- a role that bypasses RLS when reading profiles (standard Supabase pattern).
-- auth.uid() inside the function still reflects the JWT caller.
-- =============================================================================

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

COMMENT ON FUNCTION public.current_user_can_insert_lookup_rows() IS
  'True if JWT subject has organizer/admin role in public.profiles (for areas/categories INSERT RLS).';

REVOKE ALL ON FUNCTION public.current_user_can_insert_lookup_rows() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.current_user_can_insert_lookup_rows() TO authenticated;
GRANT EXECUTE ON FUNCTION public.current_user_can_insert_lookup_rows() TO service_role;

-- Recreate policies to use the helper (idempotent).
DROP POLICY IF EXISTS areas_insert_authenticated ON public.areas;
DROP POLICY IF EXISTS areas_insert_organizer ON public.areas;
CREATE POLICY areas_insert_organizer ON public.areas
  FOR INSERT TO authenticated
  WITH CHECK (
    auth.uid() IS NOT NULL
    AND public.current_user_can_insert_lookup_rows()
  );

DROP POLICY IF EXISTS categories_insert_authenticated ON public.categories;
DROP POLICY IF EXISTS categories_insert_organizer ON public.categories;
CREATE POLICY categories_insert_organizer ON public.categories
  FOR INSERT TO authenticated
  WITH CHECK (
    auth.uid() IS NOT NULL
    AND public.current_user_can_insert_lookup_rows()
  );

GRANT INSERT ON TABLE public.areas TO authenticated;
GRANT INSERT ON TABLE public.categories TO authenticated;
