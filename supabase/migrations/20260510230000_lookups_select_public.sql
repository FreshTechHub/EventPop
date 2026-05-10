-- =============================================================================
-- 20260510230000_lookups_select_public.sql
-- -----------------------------------------------------------------------------
-- Ensures public reads of public.areas / public.categories work for both
-- anon and authenticated callers. Earlier RBAC iterations dropped/replaced
-- INSERT policies; in some projects the SELECT policy went missing too,
-- which makes the mobile category dropdown fetch return zero rows even
-- though the table is populated.
-- Idempotent.
-- =============================================================================

ALTER TABLE public.areas      ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;

GRANT SELECT ON TABLE public.areas      TO anon, authenticated;
GRANT SELECT ON TABLE public.categories TO anon, authenticated;

DROP POLICY IF EXISTS areas_select_public ON public.areas;
CREATE POLICY areas_select_public ON public.areas
  FOR SELECT TO anon, authenticated
  USING (true);

DROP POLICY IF EXISTS categories_select_public ON public.categories;
CREATE POLICY categories_select_public ON public.categories
  FOR SELECT TO anon, authenticated
  USING (true);

COMMENT ON POLICY areas_select_public      ON public.areas      IS 'Public read for lookup dropdowns.';
COMMENT ON POLICY categories_select_public ON public.categories IS 'Public read for lookup dropdowns.';
