
CREATE TABLE public.areas (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  name text NOT NULL UNIQUE,
  CONSTRAINT areas_pkey PRIMARY KEY (id)
);
CREATE TABLE public.categories (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  name text NOT NULL UNIQUE,
  CONSTRAINT categories_pkey PRIMARY KEY (id)
);
CREATE TABLE public.event_interests (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  event_id uuid NOT NULL,
  user_id uuid NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
  CONSTRAINT event_interests_pkey PRIMARY KEY (id),
  CONSTRAINT event_interests_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.events(id),
  CONSTRAINT event_interests_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.profiles(id)
);
CREATE TABLE public.event_ratings (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  event_id uuid NOT NULL,
  user_id uuid NOT NULL,
  score smallint NOT NULL CHECK (score >= 1 AND score <= 5),
  created_at timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
  CONSTRAINT event_ratings_pkey PRIMARY KEY (id),
  CONSTRAINT event_ratings_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.events(id),
  CONSTRAINT event_ratings_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.profiles(id)
);
CREATE TABLE public.event_rsvps (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  event_id uuid,
  user_id uuid,
  created_at timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
  CONSTRAINT event_rsvps_pkey PRIMARY KEY (id),
  CONSTRAINT event_rsvps_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.events(id),
  CONSTRAINT event_rsvps_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.events (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  title text NOT NULL,
  location text NOT NULL,
  is_free boolean NOT NULL DEFAULT false,
  description text NOT NULL,
  rsvp_count integer NOT NULL DEFAULT 0,
  image_url text,
  created_at timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
  area_id uuid,
  category_id uuid,
  organizer_id uuid,
  avg_rating numeric NOT NULL DEFAULT 0.0,
  updated_at timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
  price numeric,
  date date,
  start_time time without time zone,
  end_time time without time zone,
  latitude double precision,
  longitude double precision,
  created_by uuid,
  CONSTRAINT events_pkey PRIMARY KEY (id),
  CONSTRAINT fk_events_area FOREIGN KEY (area_id) REFERENCES public.areas(id),
  CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES public.categories(id),
  CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES public.profiles(id),
  CONSTRAINT events_created_by_fkey FOREIGN KEY (created_by) REFERENCES auth.users(id)
);
CREATE TABLE public.profiles (
  id uuid NOT NULL,
  username text UNIQUE,
  full_name text,
  avatar_url text,
  bio text,
  phone text,
  created_at timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
  updated_at timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
  subscription_active boolean NOT NULL DEFAULT false,
  subscription_expires_at timestamp with time zone,
  role public.user_role NOT NULL DEFAULT 'user',
  CONSTRAINT profiles_pkey PRIMARY KEY (id),
  CONSTRAINT profiles_id_fkey FOREIGN KEY (id) REFERENCES auth.users(id)
);

-- Enum for RBAC. New signups default to 'user'; organizers/admins are seeded
-- via supabase/scripts/seed_organizers.sql.
-- CREATE TYPE public.user_role AS ENUM ('user', 'organizer', 'admin');