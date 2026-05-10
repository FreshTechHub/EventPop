-- =============================================================================
-- seed_organizers.sql
-- -----------------------------------------------------------------------------
-- PURPOSE: Promote ALREADY-EXISTING auth users to the `organizer` role.
--
-- This script does NOT create users. It only updates `public.profiles.role`.
-- If a target email is not in `auth.users` you'll see a NOTICE and the script
-- will skip it. To create new users from scratch (dev only), use
-- `seed_dev_organizers.sql` instead.
--
-- Run with the Supabase SQL editor or psql while connected as `postgres`
-- (service role). Idempotent: re-running has no extra effect.
-- =============================================================================

DO $$
DECLARE
    target_email   text;
    target_user_id uuid;
    target_role    public.user_role;
    promoted_count int := 0;
    -- ---------------------------------------------------------------
    -- EDIT THIS LIST: emails of users you want to promote to organizer
    -- ---------------------------------------------------------------
    target_emails text[] := ARRAY[
        'awongo@gmail.com',
        'apexgeeks8@gmail.com'
    ];
BEGIN
    FOREACH target_email IN ARRAY target_emails
    LOOP
        SELECT u.id INTO target_user_id
        FROM auth.users u
        WHERE lower(u.email) = lower(target_email)
        LIMIT 1;

        IF target_user_id IS NULL THEN
            RAISE NOTICE '[skip] % — no row in auth.users (sign this email up first, or use seed_dev_organizers.sql)', target_email;
            CONTINUE;
        END IF;

        SELECT p.role INTO target_role
        FROM public.profiles p
        WHERE p.id = target_user_id;

        IF target_role IS NULL THEN
            RAISE NOTICE '[skip] % — auth user exists but no public.profiles row (handle_new_user trigger missed?)', target_email;
            CONTINUE;
        END IF;

        IF target_role = 'admin'::public.user_role THEN
            RAISE NOTICE '[skip] % — already admin (never demoted)', target_email;
            CONTINUE;
        END IF;

        IF target_role = 'organizer'::public.user_role THEN
            RAISE NOTICE '[noop] % — already organizer', target_email;
            CONTINUE;
        END IF;

        UPDATE public.profiles
        SET role = 'organizer'::public.user_role
        WHERE id = target_user_id;

        promoted_count := promoted_count + 1;
        RAISE NOTICE '[ok]   % — promoted user → organizer', target_email;
    END LOOP;

    RAISE NOTICE '----------------------------------------';
    RAISE NOTICE 'Done. Promoted % user(s) to organizer.', promoted_count;
END
$$;

-- -----------------------------------------------------------------------------
-- Optional: promote a single admin (dev/ops only). Uncomment as needed.
-- -----------------------------------------------------------------------------
-- UPDATE public.profiles
-- SET role = 'admin'::public.user_role
-- WHERE id = (SELECT id FROM auth.users WHERE lower(email) = lower('admin@example.com'));

-- -----------------------------------------------------------------------------
-- Optional: revoke organizer back to user. Uncomment + edit emails to use.
-- -----------------------------------------------------------------------------
-- UPDATE public.profiles
-- SET role = 'user'::public.user_role
-- WHERE id IN (
--   SELECT id FROM auth.users
--   WHERE lower(email) = ANY (ARRAY[
--     lower('former-organizer@example.com')
--   ])
-- ) AND role = 'organizer'::public.user_role;
