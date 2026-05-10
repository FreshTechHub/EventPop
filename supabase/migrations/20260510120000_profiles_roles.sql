
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
  ADD COLUMN IF NOT EXISTS role public.user_role NOT NULL DEFAULT 'user';

CREATE INDEX IF NOT EXISTS idx_profiles_role ON public.profiles (role);

COMMENT ON COLUMN public.profiles.role IS
  'RBAC role. Only organizer/admin can create events. New signups default to user.';

-- ---------------------------------------------------------------------------
-- get_my_role: small RPC for the mobile client to discover its own role.
-- SECURITY DEFINER is safe here because it only references auth.uid().
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
