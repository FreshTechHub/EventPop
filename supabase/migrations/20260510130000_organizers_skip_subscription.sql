-- =============================================================================
-- 20260510130000_organizers_skip_subscription.sql
-- -----------------------------------------------------------------------------
-- RBAC simplification: organizers (and admins) may create unlimited events
-- regardless of subscription status. Only `user`-role accounts are blocked.
--
-- Previous behaviour gated organizers behind:
--     role IN ('organizer','admin')
--     AND (subscription_active OR hosted_event_count < 2)
--
-- New behaviour:
--     role IN ('organizer','admin')
--
-- The subscription / quota fields stay in the schema and the host-quota RPC
-- (so the UI can keep showing a counter), but they no longer block inserts.
-- =============================================================================

CREATE OR REPLACE FUNCTION public.user_can_create_event()
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY INVOKER
SET search_path = public
AS $$
  SELECT EXISTS (
    SELECT 1
    FROM public.profiles p
    WHERE p.id = auth.uid()
      AND p.role IN ('organizer'::public.user_role, 'admin'::public.user_role)
  );
$$;

COMMENT ON FUNCTION public.user_can_create_event() IS
  'True iff the caller has organizer/admin role. Subscription/quota are ignored.';
