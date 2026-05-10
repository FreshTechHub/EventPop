-- =============================================================================
-- seed_dev_organizers.sql      DEV / LOCAL ONLY — DO NOT RUN IN PRODUCTION
-- -----------------------------------------------------------------------------
-- Creates organizer accounts end-to-end:
--   1. Inserts into auth.users (email-confirmed, bcrypt-hashed password).
--   2. Inserts the matching auth.identities row so password login works.
--   3. The on_auth_user_created trigger creates public.profiles automatically.
--   4. Promotes those profiles to role = 'organizer'.
--
-- WHY DEV ONLY:
--   * Direct INSERTs into auth.users bypass Supabase's email/SMS verification,
--     captcha, rate-limiting, and audit hooks.
--   * The shape of auth.users / auth.identities can change between Supabase
--     releases. If this script errors out, fall back to creating users via the
--     Supabase Dashboard ("Authentication → Users → Add user") and then run
--     `seed_organizers.sql` to promote them.
--
-- USAGE:
--   * Run as the `postgres` role in the SQL editor (uses pgcrypto for hashing).
--   * Edit the `target_users` array below before running.
--   * Idempotent: re-running with the same email is a no-op (will only ensure
--     the role is `organizer` and never demote an admin).
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA extensions;

DO $$
DECLARE
    new_user_id       uuid;
    existing_user_id  uuid;
    existing_role     public.user_role;
    created_count     int := 0;
    promoted_count    int := 0;

    -- ---------------------------------------------------------------
    -- EDIT: organizers to seed.  (email, password, full_name)
    -- Use STRONG, EPHEMERAL passwords. Rotate them after first login.
    -- ---------------------------------------------------------------
    target_users text[][] := ARRAY[
        ARRAY['awongo@gmail.com',  'Organizer#2026!', 'EventPop Organizer'],
        ARRAY['apexgeeks8@gmail.com',    'Organizer#2026!', 'A. Wongo']
    ];
BEGIN
    FOR i IN 1..array_length(target_users, 1) LOOP
        DECLARE
            target_email    text := lower(trim(target_users[i][1]));
            target_password text := target_users[i][2];
            target_name     text := target_users[i][3];
        BEGIN
            -- Reset per-iteration state defensively (SELECT INTO already nulls
            -- on no-match, but explicit reset makes the intent obvious).
            existing_user_id := NULL;
            existing_role := NULL;
            new_user_id := NULL;

            -- 1) If auth user already exists, only ensure organizer role.
            SELECT id INTO existing_user_id
            FROM auth.users
            WHERE lower(email) = target_email
            LIMIT 1;

            IF existing_user_id IS NOT NULL THEN
                SELECT role INTO existing_role FROM public.profiles WHERE id = existing_user_id;

                IF existing_role IS NULL THEN
                    -- auth user exists but no profile row (trigger missed?). Fix it.
                    INSERT INTO public.profiles (id, username, full_name, role)
                    VALUES (
                        existing_user_id,
                        left(regexp_replace(split_part(target_email, '@', 1), '[^a-zA-Z0-9_]', '', 'g'), 30),
                        target_name,
                        'organizer'::public.user_role
                    )
                    ON CONFLICT (id) DO UPDATE
                        SET role = 'organizer'::public.user_role
                        WHERE public.profiles.role <> 'admin'::public.user_role;
                    promoted_count := promoted_count + 1;
                    RAISE NOTICE '[fix  ] % — auth user existed, profile created as organizer', target_email;
                ELSIF existing_role = 'admin'::public.user_role THEN
                    RAISE NOTICE '[skip ] % — already admin (left untouched)', target_email;
                ELSIF existing_role = 'organizer'::public.user_role THEN
                    RAISE NOTICE '[noop ] % — already organizer', target_email;
                ELSE
                    UPDATE public.profiles
                    SET role = 'organizer'::public.user_role
                    WHERE id = existing_user_id;
                    promoted_count := promoted_count + 1;
                    RAISE NOTICE '[promo] % — existed, promoted user → organizer', target_email;
                END IF;
                CONTINUE;
            END IF;

            -- 2) Create new auth user.
            new_user_id := gen_random_uuid();

            INSERT INTO auth.users (
                instance_id,
                id,
                aud,
                role,
                email,
                encrypted_password,
                email_confirmed_at,
                raw_app_meta_data,
                raw_user_meta_data,
                created_at,
                updated_at,
                confirmation_token,
                email_change,
                email_change_token_new,
                recovery_token
            ) VALUES (
                '00000000-0000-0000-0000-000000000000'::uuid,
                new_user_id,
                'authenticated',
                'authenticated',
                target_email,
                extensions.crypt(target_password, extensions.gen_salt('bf')),
                now(),
                jsonb_build_object('provider', 'email', 'providers', jsonb_build_array('email')),
                jsonb_build_object('full_name', target_name),
                now(),
                now(),
                '',
                '',
                '',
                ''
            );

            -- 3) Create matching identity row so password login works.
            INSERT INTO auth.identities (
                provider_id,
                user_id,
                identity_data,
                provider,
                last_sign_in_at,
                created_at,
                updated_at
            ) VALUES (
                target_email,
                new_user_id,
                jsonb_build_object(
                    'sub',           new_user_id::text,
                    'email',         target_email,
                    'email_verified', true,
                    'provider',      'email'
                ),
                'email',
                now(),
                now(),
                now()
            );

            -- 4) The on_auth_user_created trigger should have inserted into
            --    public.profiles. Promote that row to organizer; if the trigger
            --    is missing/disabled, create the profile row ourselves so the
            --    user can sign in and be recognised as an organizer.
            UPDATE public.profiles
            SET role = 'organizer'::public.user_role
            WHERE id = new_user_id;

            IF NOT FOUND THEN
                INSERT INTO public.profiles (id, username, full_name, role)
                VALUES (
                    new_user_id,
                    left(regexp_replace(split_part(target_email, '@', 1), '[^a-zA-Z0-9_]', '', 'g'), 30),
                    target_name,
                    'organizer'::public.user_role
                )
                ON CONFLICT (id) DO UPDATE
                    SET role = 'organizer'::public.user_role
                    WHERE public.profiles.role <> 'admin'::public.user_role;
                RAISE NOTICE '[new  ] % — created auth user + profile (trigger missed, inserted directly)', target_email;
            ELSE
                RAISE NOTICE '[new  ] % — created auth user + organizer profile', target_email;
            END IF;

            created_count := created_count + 1;
        END;
    END LOOP;

    RAISE NOTICE '----------------------------------------';
    RAISE NOTICE 'Created % new organizer(s); promoted % existing user(s).',
        created_count, promoted_count;
    RAISE NOTICE 'Sign in with the configured passwords. Rotate them after first login.';
END
$$;
