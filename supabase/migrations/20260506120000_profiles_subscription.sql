-- EventPop: subscription gate for hosts who exceed the free event limit.
-- Flip subscription_active (e.g. via Stripe/RevenueCat webhook or SQL) to allow more than 2 hosted events.

ALTER TABLE public.profiles
  ADD COLUMN IF NOT EXISTS subscription_active boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS subscription_expires_at timestamptz;

COMMENT ON COLUMN public.profiles.subscription_active IS 'When true, user is not limited to 2 created events.';
COMMENT ON COLUMN public.profiles.subscription_expires_at IS 'Optional: if set in app logic, subscription_active should follow this timestamp.';
