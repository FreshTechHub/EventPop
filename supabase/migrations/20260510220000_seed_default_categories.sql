-- =============================================================================
-- 20260510220000_seed_default_categories.sql
-- -----------------------------------------------------------------------------
-- Seed the EventPop default categories. Idempotent — only inserts rows that
-- aren't already present (case-insensitive). The names match
-- com.android.example.eventpop.data.EventCategory.displayName so the
-- EventRemoteMapper can colour markers correctly.
--
-- Adding a new category is purely a DB operation:
--     INSERT INTO public.categories (name) VALUES ('Tech');
-- =============================================================================

INSERT INTO public.categories (name)
SELECT v
FROM (VALUES
  ('Music'),
  ('Venue'),
  ('Comedy'),
  ('Wellness'),
  ('Art'),
  ('Food')
) AS x(v)
WHERE NOT EXISTS (
  SELECT 1 FROM public.categories c
  WHERE lower(c.name) = lower(x.v)
);
