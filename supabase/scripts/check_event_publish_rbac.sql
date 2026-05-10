-- =============================================================================
-- check_event_publish_rbac.sql
-- -----------------------------------------------------------------------------
-- Pure-diagnostic, read-only. Designed for the Supabase SQL editor, which
-- hides RAISE NOTICE output. Every block is a SELECT, so each result appears
-- in the editor's results pane with a clear status column.
--
-- Run all six SELECTs (the editor runs them top-to-bottom and shows the last
-- one; if you only see one result, run them one at a time, or copy each into
-- its own tab).
--
-- The repair scripts at the bottom are commented out — read the diagnostic
-- output first, then uncomment the matching block.
-- =============================================================================

-- 1. Preflight: does the role enum + profiles.role column exist?
SELECT
    EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') AS user_role_enum,
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'profiles' AND column_name = 'role'
    ) AS profiles_role_column,
    CASE
        WHEN EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role')
         AND EXISTS (
             SELECT 1 FROM information_schema.columns
             WHERE table_schema = 'public' AND table_name = 'profiles' AND column_name = 'role'
         )
        THEN 'OK'
        ELSE 'BLOCKED: apply 20260510120000_profiles_roles.sql first'
    END AS status;

-- 2. Policies on public.areas. The role-aware migration must add a row named
--    areas_insert_organizer with cmd='a' (INSERT) and a check expression that
--    references public.profiles.role.
SELECT
    polname                                       AS policy_name,
    polcmd                                        AS cmd,
    pg_get_expr(polqual,       polrelid)          AS using_expr,
    pg_get_expr(polwithcheck,  polrelid)          AS check_expr
FROM pg_policy
WHERE polrelid = 'public.areas'::regclass
ORDER BY polname;

-- 3. Same for public.categories.
SELECT
    polname                                       AS policy_name,
    polcmd                                        AS cmd,
    pg_get_expr(polqual,       polrelid)          AS using_expr,
    pg_get_expr(polwithcheck,  polrelid)          AS check_expr
FROM pg_policy
WHERE polrelid = 'public.categories'::regclass
ORDER BY polname;

-- 4. Quick verdict for the lookup-insert migration.
SELECT
    EXISTS (
        SELECT 1 FROM pg_policy
        WHERE polrelid = 'public.areas'::regclass
          AND polname = 'areas_insert_organizer'
    ) AS areas_insert_organizer_present,
    EXISTS (
        SELECT 1 FROM pg_policy
        WHERE polrelid = 'public.categories'::regclass
          AND polname = 'categories_insert_organizer'
    ) AS categories_insert_organizer_present,
    CASE
        WHEN EXISTS (
            SELECT 1 FROM pg_policy
            WHERE polrelid = 'public.areas'::regclass
              AND polname = 'areas_insert_organizer'
        )
        THEN 'OK'
        ELSE 'BLOCKED: apply 20260510140000_lookups_insert_organizer_only.sql'
    END AS status;

-- 5. Body of public.user_can_create_event() — must reference profiles.role.
SELECT
    proname                          AS function_name,
    pg_get_functiondef(oid)          AS definition
FROM pg_proc
WHERE proname = 'user_can_create_event'
  AND pronamespace = 'public'::regnamespace;

-- 6. Every organizer / admin currently in the database.
SELECT
    u.email,
    p.role::text AS role,
    p.id,
    p.subscription_active,
    p.created_at
FROM public.profiles p
JOIN auth.users u ON u.id = p.id
WHERE p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
ORDER BY p.role, u.email;

-- =============================================================================
-- Repairs (uncomment what's needed based on the SELECT outputs above)
-- =============================================================================

-- A) Re-apply the role-aware lookup-insert policies. Idempotent.
--
-- DROP POLICY IF EXISTS areas_insert_authenticated ON public.areas;
-- DROP POLICY IF EXISTS areas_insert_organizer    ON public.areas;
-- CREATE POLICY areas_insert_organizer ON public.areas
--   FOR INSERT TO authenticated
--   WITH CHECK (
--     auth.uid() IS NOT NULL
--     AND EXISTS (
--       SELECT 1 FROM public.profiles p
--       WHERE p.id = auth.uid()
--         AND p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
--     )
--   );
-- DROP POLICY IF EXISTS categories_insert_authenticated ON public.categories;
-- DROP POLICY IF EXISTS categories_insert_organizer    ON public.categories;
-- CREATE POLICY categories_insert_organizer ON public.categories
--   FOR INSERT TO authenticated
--   WITH CHECK (
--     auth.uid() IS NOT NULL
--     AND EXISTS (
--       SELECT 1 FROM public.profiles p
--       WHERE p.id = auth.uid()
--         AND p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
--     )
--   );
-- GRANT INSERT ON TABLE public.areas      TO authenticated;
-- GRANT INSERT ON TABLE public.categories TO authenticated;

-- B) Promote a specific account to organizer. Replace the email below.
--
-- UPDATE public.profiles
-- SET role = 'organizer'::public.user_role
-- WHERE id = (
--     SELECT id FROM auth.users WHERE lower(email) = lower('your-test@example.com')
-- )
-- RETURNING id, role;
