-- Align public.events with Android EventRemoteRow / PostgREST selects.
-- Run in Supabase SQL Editor (or CLI) after your base EventPop schema exists.
--
-- Fixes:
--   - Adds latitude / longitude (missing from DB dump vs app model).
--
-- Not changed here (usually fine with PostgREST JSON + Kotlin String?):
--   - date = date, start_time / end_time = time — PostgREST typically returns
--     quoted strings in JSON; if decoding fails in the app, consider a follow-up
--     migration to text columns and a USING clause to format existing values.

ALTER TABLE public.events
  ADD COLUMN IF NOT EXISTS latitude double precision,
  ADD COLUMN IF NOT EXISTS longitude double precision;

COMMENT ON COLUMN public.events.latitude IS 'Optional map pin; used by EventRemoteRow.';
COMMENT ON COLUMN public.events.longitude IS 'Optional map pin; used by EventRemoteRow.';
