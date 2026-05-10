-- =============================================================================
-- 20260510210000_lookup_upsert_rpcs.sql
-- -----------------------------------------------------------------------------
-- Bullet-proof lookup-insert path for the mobile Create Event flow.
--
-- Problem:
--   Even after RLS policies on public.areas / public.categories are correctly
--   gated by user_can_create_event(), some Postgres + Supabase combinations
--   still reject INSERTs from authenticated organizers (42501). Reproducing it
--   reliably has been a moving target.
--
-- Solution:
--   Provide SECURITY DEFINER RPCs that:
--     • check the caller's role explicitly,
--     • upsert the lookup row by case-insensitive name,
--     • return the row id.
--   The RPC is owned by `postgres` so it bypasses RLS on the underlying table.
--   The role check inside guarantees only organizers/admins can create rows.
--
-- Also adds case-insensitive unique indexes to make the upsert race-safe and
-- stop "Koboko"/"koboko" from creating duplicates.
-- =============================================================================

-- 1. Case-insensitive uniqueness on lookup names.
CREATE UNIQUE INDEX IF NOT EXISTS uq_areas_name_ci
  ON public.areas (lower(name));

CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_name_ci
  ON public.categories (lower(name));

-- 2. ensure_area: upsert by name, return id. Organizer/admin only.
CREATE OR REPLACE FUNCTION public.ensure_area(p_name text)
RETURNS uuid
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  v_name text := trim(p_name);
  v_id   uuid;
BEGIN
  IF v_name = '' OR v_name IS NULL THEN
    RAISE EXCEPTION 'Area name is required' USING ERRCODE = '22023';
  END IF;
  IF NOT public.user_can_create_event() THEN
    RAISE EXCEPTION 'Only organizers may add areas' USING ERRCODE = '42501';
  END IF;

  SELECT id INTO v_id
  FROM public.areas
  WHERE lower(name) = lower(v_name)
  LIMIT 1;

  IF v_id IS NOT NULL THEN
    RETURN v_id;
  END IF;

  INSERT INTO public.areas (name)
  VALUES (v_name)
  RETURNING id INTO v_id;

  RETURN v_id;

EXCEPTION
  WHEN unique_violation THEN
    -- Concurrent insert won the race; return the existing row.
    SELECT id INTO v_id
    FROM public.areas
    WHERE lower(name) = lower(v_name)
    LIMIT 1;
    RETURN v_id;
END;
$$;

COMMENT ON FUNCTION public.ensure_area(text) IS
  'Returns id for the matching public.areas row, creating it if missing. Organizer/admin only.';

REVOKE ALL ON FUNCTION public.ensure_area(text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.ensure_area(text) TO authenticated;
GRANT EXECUTE ON FUNCTION public.ensure_area(text) TO service_role;

-- 3. ensure_category: same pattern. The mobile app no longer inserts categories
--    in the publish path (the dropdown is DB-driven), but the RPC stays for
--    admin/seed tools.
CREATE OR REPLACE FUNCTION public.ensure_category(p_name text)
RETURNS uuid
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  v_name text := trim(p_name);
  v_id   uuid;
BEGIN
  IF v_name = '' OR v_name IS NULL THEN
    RAISE EXCEPTION 'Category name is required' USING ERRCODE = '22023';
  END IF;
  IF NOT public.user_can_create_event() THEN
    RAISE EXCEPTION 'Only organizers may add categories' USING ERRCODE = '42501';
  END IF;

  SELECT id INTO v_id
  FROM public.categories
  WHERE lower(name) = lower(v_name)
  LIMIT 1;

  IF v_id IS NOT NULL THEN
    RETURN v_id;
  END IF;

  INSERT INTO public.categories (name)
  VALUES (v_name)
  RETURNING id INTO v_id;

  RETURN v_id;

EXCEPTION
  WHEN unique_violation THEN
    SELECT id INTO v_id
    FROM public.categories
    WHERE lower(name) = lower(v_name)
    LIMIT 1;
    RETURN v_id;
END;
$$;

COMMENT ON FUNCTION public.ensure_category(text) IS
  'Returns id for the matching public.categories row, creating it if missing. Organizer/admin only.';

REVOKE ALL ON FUNCTION public.ensure_category(text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.ensure_category(text) TO authenticated;
GRANT EXECUTE ON FUNCTION public.ensure_category(text) TO service_role;
