-- EventPop: track event hosts, cap free tier at 2 events per user, allow unlimited when subscribed.
-- created_by is set automatically from the session (clients cannot spoof another user).

ALTER TABLE public.events
  ADD COLUMN IF NOT EXISTS created_by uuid REFERENCES auth.users (id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_events_created_by ON public.events (created_by);

COMMENT ON COLUMN public.events.created_by IS 'Host user; null on legacy rows predating this column.';

-- Free tier: fewer than 2 rows with this creator, OR profile.subscription_active.
CREATE OR REPLACE FUNCTION public.user_can_create_event()
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY INVOKER
SET search_path = public
AS $$
  SELECT COALESCE(
    (SELECT p.subscription_active FROM public.profiles p WHERE p.id = auth.uid()),
    false
  )
  OR (
    SELECT COUNT(*)::bigint FROM public.events e WHERE e.created_by = auth.uid()
  ) < 2;
$$;

COMMENT ON FUNCTION public.user_can_create_event() IS 'True if caller may INSERT into public.events (subscribed or under 2 hosted events).';

CREATE OR REPLACE FUNCTION public.events_set_created_by()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  NEW.created_by := auth.uid();
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS events_set_created_by ON public.events;
CREATE TRIGGER events_set_created_by
  BEFORE INSERT ON public.events
  FOR EACH ROW
  EXECUTE FUNCTION public.events_set_created_by();

GRANT INSERT, UPDATE, DELETE ON TABLE public.events TO authenticated;

DROP POLICY IF EXISTS events_insert_tier ON public.events;
CREATE POLICY events_insert_tier ON public.events
  FOR INSERT TO authenticated
  WITH CHECK (
    created_by = auth.uid()
    AND auth.uid() IS NOT NULL
    AND public.user_can_create_event()
  );

DROP POLICY IF EXISTS events_update_own ON public.events;
CREATE POLICY events_update_own ON public.events
  FOR UPDATE TO authenticated
  USING (created_by = auth.uid())
  WITH CHECK (created_by = auth.uid());

DROP POLICY IF EXISTS events_delete_own ON public.events;
CREATE POLICY events_delete_own ON public.events
  FOR DELETE TO authenticated
  USING (created_by = auth.uid());
