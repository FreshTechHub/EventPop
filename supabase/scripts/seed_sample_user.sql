-- =============================================================================
-- EventPop — seed one email/password user (auth.users + auth.identities)
-- =============================================================================
-- Run in Supabase SQL Editor (Dashboard → SQL) or `psql` as a role that can
-- write to auth.* (local `supabase db reset` / SQL editor on hosted projects).
--
-- Default login after seed (edit DECLARE below if you like):
--   Email:    sample.user@eventpop.test
--   Password: EventPop.Sample123
--
-- Idempotent: skips entirely if that email OR that fixed id already exists in
-- auth.users (no orphan public.profiles insert).
--
-- public.profiles: trigger handle_new_user usually creates the row; the
-- INSERT here runs only after a successful auth seed and uses ON CONFLICT
-- DO NOTHING as a fallback.
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO
$$
DECLARE
  seed_email text := 'awongo@gmail.com';
  seed_password text := '12345678';
  seed_user_id uuid := 'f0000000-0000-4000-8000-000000000001';
  encrypted_pw text;
BEGIN
  IF EXISTS (SELECT 1 FROM auth.users WHERE email = seed_email) THEN
    RAISE NOTICE 'Seed skipped: user with email % already exists.', seed_email;
    RETURN;
  END IF;

  IF EXISTS (SELECT 1 FROM auth.users WHERE id = seed_user_id) THEN
    RAISE NOTICE 'Seed skipped: auth.users id % already exists.', seed_user_id;
    RETURN;
  END IF;

  encrypted_pw := crypt(seed_password, gen_salt('bf'));

  INSERT INTO auth.users (
    id,
    instance_id,
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
    recovery_token,
    is_sso_user,
    is_anonymous
  )
  VALUES (
    seed_user_id,
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    seed_email,
    encrypted_pw,
    timezone('utc', now()),
    '{"provider":"email","providers":["email"]}'::jsonb,
    jsonb_build_object('full_name', 'Sample User'),
    timezone('utc', now()),
    timezone('utc', now()),
    '',
    '',
    '',
    '',
    false,
    false
  );

  INSERT INTO auth.identities (
    id,
    user_id,
    identity_data,
    provider,
    provider_id,
    last_sign_in_at,
    created_at,
    updated_at
  )
  VALUES (
    gen_random_uuid(),
    seed_user_id,
    jsonb_build_object(
      'sub', seed_user_id::text,
      'email', seed_email
    ),
    'email',
    seed_user_id::text,
    timezone('utc', now()),
    timezone('utc', now()),
    timezone('utc', now())
  );

  -- Only after auth row exists (never run when RETURN skipped auth insert).
  INSERT INTO public.profiles (id, username, full_name)
  VALUES (seed_user_id, 'sampleuser', 'Sample User')
  ON CONFLICT (id) DO NOTHING;
END
$$;
