-- Harden host quota + insert gate: explicit grouping for OR / comparison, and a single RPC
-- the mobile client can call so counts match server-side RLS/insert checks.

-- ---------------------------------------------------------------------------
-- Insert policy helper: subscribed OR fewer than 2 hosted events (explicit parens).
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.user_can_create_event()
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY INVOKER
SET search_path = public
AS $$
  SELECT (
    COALESCE(
      (SELECT p.subscription_active FROM public.profiles p WHERE p.id = auth.uid()),
      false
    )
    OR (
      (SELECT COUNT(*)::bigint FROM public.events e WHERE e.created_by = auth.uid()) < 2
    )
  );
$$;

COMMENT ON FUNCTION public.user_can_create_event() IS
  'True if caller may INSERT into public.events (subscribed or under 2 hosted events).';

-- ---------------------------------------------------------------------------
-- RPC: subscription flag + hosted count for auth.uid() (SECURITY DEFINER).
-- Uses only auth.uid() — safe for authenticated callers.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.get_host_event_quota()
RETURNS TABLE(subscription_active boolean, hosted_event_count bigint)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT
    COALESCE(
      (SELECT p.subscription_active FROM public.profiles p WHERE p.id = auth.uid()),
      false
    ) AS subscription_active,
    COALESCE(
      (SELECT COUNT(*)::bigint FROM public.events e WHERE e.created_by = auth.uid()),
      0::bigint
    ) AS hosted_event_count;
$$;

COMMENT ON FUNCTION public.get_host_event_quota() IS
  'Returns subscription_active and count of events hosted by the current user (for create-event UI).';

REVOKE ALL ON FUNCTION public.get_host_event_quota() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.get_host_event_quota() TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_host_event_quota() TO service_role;
