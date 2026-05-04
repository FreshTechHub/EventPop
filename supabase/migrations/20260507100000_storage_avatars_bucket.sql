-- EventPop: storage bucket `avatars` for profile pictures (matches StorageBuckets.AVATARS).
-- Object keys: public/<auth.uid>/avatar.jpg — public read, authenticated write/delete own prefix.

CREATE OR REPLACE FUNCTION public.bootstrap_avatars_bucket()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = storage, public
AS $$
BEGIN
  INSERT INTO storage.buckets (id, name, public)
  VALUES ('avatars', 'avatars', true)
  ON CONFLICT (id) DO NOTHING;
END;
$$;

DO $$
BEGIN
  PERFORM public.bootstrap_avatars_bucket();
END $$;

CREATE OR REPLACE FUNCTION public.trg_profiles_ensure_avatars_bucket()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  PERFORM public.bootstrap_avatars_bucket();
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS profiles_ensure_avatars_bucket ON public.profiles;
CREATE TRIGGER profiles_ensure_avatars_bucket
  AFTER INSERT ON public.profiles
  FOR EACH ROW
  EXECUTE FUNCTION public.trg_profiles_ensure_avatars_bucket();

DROP POLICY IF EXISTS avatars_objects_select_public ON storage.objects;
CREATE POLICY avatars_objects_select_public ON storage.objects
  FOR SELECT TO anon, authenticated
  USING (bucket_id = 'avatars');

DROP POLICY IF EXISTS avatars_objects_insert_own_public_prefix ON storage.objects;
CREATE POLICY avatars_objects_insert_own_public_prefix ON storage.objects
  FOR INSERT TO authenticated
  WITH CHECK (
    bucket_id = 'avatars'
    AND auth.uid() IS NOT NULL
    AND name LIKE ('public/' || auth.uid()::text || '/%')
  );

DROP POLICY IF EXISTS avatars_objects_update_own_public_prefix ON storage.objects;
CREATE POLICY avatars_objects_update_own_public_prefix ON storage.objects
  FOR UPDATE TO authenticated
  USING (
    bucket_id = 'avatars'
    AND auth.uid() IS NOT NULL
    AND name LIKE ('public/' || auth.uid()::text || '/%')
  )
  WITH CHECK (
    bucket_id = 'avatars'
    AND auth.uid() IS NOT NULL
    AND name LIKE ('public/' || auth.uid()::text || '/%')
  );

DROP POLICY IF EXISTS avatars_objects_delete_own_public_prefix ON storage.objects;
CREATE POLICY avatars_objects_delete_own_public_prefix ON storage.objects
  FOR DELETE TO authenticated
  USING (
    bucket_id = 'avatars'
    AND auth.uid() IS NOT NULL
    AND name LIKE ('public/' || auth.uid()::text || '/%')
  );
