-- EventPop: ensure storage bucket `event-images` (matches Android StorageBuckets.EVENT_IMAGES).
-- Idempotent bucket insert + RLS on storage.objects for public reads and per-user upload paths.
-- AFTER INSERT on public.profiles runs bootstrap so new signups always converge on a bucket
-- even if the bucket row was missing when this migration first ran.

CREATE OR REPLACE FUNCTION public.bootstrap_event_images_bucket()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = storage, public
AS $$
BEGIN
  INSERT INTO storage.buckets (id, name, public)
  VALUES ('event-images', 'event-images', true)
  ON CONFLICT (id) DO NOTHING;
END;
$$;

COMMENT ON FUNCTION public.bootstrap_event_images_bucket() IS 'Creates event-images bucket if missing (idempotent).';

CREATE OR REPLACE FUNCTION public.trg_profiles_ensure_event_images_bucket()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  PERFORM public.bootstrap_event_images_bucket();
  RETURN NEW;
END;
$$;

-- One-shot: bucket exists even before any new profile is inserted.
DO $$
BEGIN
  PERFORM public.bootstrap_event_images_bucket();
END $$;

DROP TRIGGER IF EXISTS profiles_ensure_event_images_bucket ON public.profiles;
CREATE TRIGGER profiles_ensure_event_images_bucket
  AFTER INSERT ON public.profiles
  FOR EACH ROW
  EXECUTE FUNCTION public.trg_profiles_ensure_event_images_bucket();

-- ---------------------------------------------------------------------------
-- storage.objects policies (bucket id matches app StorageBuckets.EVENT_IMAGES)
-- Object paths must start with "<auth.uid()>/" so users cannot overwrite others' keys.
-- ---------------------------------------------------------------------------

DROP POLICY IF EXISTS event_images_objects_select_public ON storage.objects;
CREATE POLICY event_images_objects_select_public ON storage.objects
  FOR SELECT TO anon, authenticated
  USING (bucket_id = 'event-images');

DROP POLICY IF EXISTS event_images_objects_insert_own_prefix ON storage.objects;
CREATE POLICY event_images_objects_insert_own_prefix ON storage.objects
  FOR INSERT TO authenticated
  WITH CHECK (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
  );

DROP POLICY IF EXISTS event_images_objects_update_own_prefix ON storage.objects;
CREATE POLICY event_images_objects_update_own_prefix ON storage.objects
  FOR UPDATE TO authenticated
  USING (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
  )
  WITH CHECK (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
  );

DROP POLICY IF EXISTS event_images_objects_delete_own_prefix ON storage.objects;
CREATE POLICY event_images_objects_delete_own_prefix ON storage.objects
  FOR DELETE TO authenticated
  USING (
    bucket_id = 'event-images'
    AND auth.uid() IS NOT NULL
    AND name LIKE (auth.uid()::text || '/%')
  );
