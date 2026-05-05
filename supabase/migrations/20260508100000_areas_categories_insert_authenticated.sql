-- Let signed-in clients add lookup rows (e.g. from a future admin UI or PostgREST).
-- SELECT policies already exist on public.areas / public.categories.

GRANT INSERT ON TABLE public.areas TO authenticated;
GRANT INSERT ON TABLE public.categories TO authenticated;

DROP POLICY IF EXISTS areas_insert_authenticated ON public.areas;
CREATE POLICY areas_insert_authenticated ON public.areas
  FOR INSERT TO authenticated
  WITH CHECK (true);

DROP POLICY IF EXISTS categories_insert_authenticated ON public.categories;
CREATE POLICY categories_insert_authenticated ON public.categories
  FOR INSERT TO authenticated
  WITH CHECK (true);
