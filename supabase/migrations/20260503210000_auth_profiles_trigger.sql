-- Idempotent auth → profiles bootstrap.
-- Runs as SECURITY DEFINER so inserts succeed regardless of caller RLS on auth.users.
-- Client inserts may race with this trigger; ON CONFLICT (id) DO NOTHING keeps inserts safe.

CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users (id) ON DELETE CASCADE,
    username TEXT UNIQUE,
    full_name TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    base_username text;
    meta_name text;
    display_name text;
BEGIN
    meta_name := NEW.raw_user_meta_data ->> 'full_name';
    display_name := COALESCE(
        NULLIF(trim(meta_name), ''),
        split_part(NEW.email, '@', 1),
        'User'
    );

    base_username := lower(
        regexp_replace(
            split_part(NEW.email, '@', 1),
            '[^a-zA-Z0-9_]',
            '',
            'g'
        )
    );

    IF base_username IS NULL OR base_username = '' THEN
        base_username := 'user';
    END IF;

    base_username := left(base_username, 24);

    INSERT INTO public.profiles (id, username, full_name)
    VALUES (NEW.id, left(base_username, 30), display_name)
    ON CONFLICT (id) DO NOTHING;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();
