-- EventPop: events feed + favorites (matches Android PostgREST usage).
-- Run in Supabase: SQL Editor → New query → paste → Run.
-- Safe to re-run: uses IF NOT EXISTS / DROP POLICY IF EXISTS where needed.

-- ---------------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS public.areas (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  name text NOT NULL,
  CONSTRAINT areas_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.categories (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  name text NOT NULL,
  CONSTRAINT categories_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.events (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  title text NOT NULL,
  location text NOT NULL,
  is_free boolean NOT NULL DEFAULT false,
  description text NOT NULL DEFAULT ''::text,
  rsvp_count integer NOT NULL DEFAULT 0,
  image_url text,
  avg_rating double precision,
  price numeric,
  date text,
  start_time text,
  end_time text,
  area_id uuid,
  category_id uuid,
  latitude double precision,
  longitude double precision,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT events_pkey PRIMARY KEY (id),
  CONSTRAINT events_area_id_fkey FOREIGN KEY (area_id) REFERENCES public.areas (id),
  CONSTRAINT events_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories (id)
);

CREATE TABLE IF NOT EXISTS public.event_interests (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  event_id uuid NOT NULL,
  user_id uuid NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT event_interests_pkey PRIMARY KEY (id),
  CONSTRAINT event_interests_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.events (id) ON DELETE CASCADE,
  CONSTRAINT event_interests_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users (id) ON DELETE CASCADE,
  CONSTRAINT event_interests_event_user_unique UNIQUE (event_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_events_area_id ON public.events (area_id);
CREATE INDEX IF NOT EXISTS idx_events_category_id ON public.events (category_id);
CREATE INDEX IF NOT EXISTS idx_event_interests_user_id ON public.event_interests (user_id);

-- ---------------------------------------------------------------------------
-- API access (PostgREST)
-- ---------------------------------------------------------------------------

GRANT SELECT ON TABLE public.areas TO anon, authenticated;
GRANT SELECT ON TABLE public.categories TO anon, authenticated;
GRANT SELECT ON TABLE public.events TO anon, authenticated;
GRANT SELECT, INSERT, DELETE ON TABLE public.event_interests TO authenticated;

-- ---------------------------------------------------------------------------
-- Row Level Security
-- ---------------------------------------------------------------------------

ALTER TABLE public.areas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.event_interests ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS areas_select_public ON public.areas;
CREATE POLICY areas_select_public ON public.areas
  FOR SELECT TO anon, authenticated
  USING (true);

DROP POLICY IF EXISTS categories_select_public ON public.categories;
CREATE POLICY categories_select_public ON public.categories
  FOR SELECT TO anon, authenticated
  USING (true);

DROP POLICY IF EXISTS events_select_public ON public.events;
CREATE POLICY events_select_public ON public.events
  FOR SELECT TO anon, authenticated
  USING (true);

DROP POLICY IF EXISTS event_interests_select_own ON public.event_interests;
CREATE POLICY event_interests_select_own ON public.event_interests
  FOR SELECT TO authenticated
  USING (auth.uid() = user_id);

DROP POLICY IF EXISTS event_interests_insert_own ON public.event_interests;
CREATE POLICY event_interests_insert_own ON public.event_interests
  FOR INSERT TO authenticated
  WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS event_interests_delete_own ON public.event_interests;
CREATE POLICY event_interests_delete_own ON public.event_interests
  FOR DELETE TO authenticated
  USING (auth.uid() = user_id);
