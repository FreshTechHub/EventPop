-- =============================================================================
-- EventPop — development sample data (Supabase SQL Editor or `psql`)
-- =============================================================================
-- Run against your **project** database (Dashboard → SQL → New query).
--
-- Expects tables: public.areas, public.categories, public.events
-- (FKs: events.area_id → areas.id, events.category_id → categories.id)
-- Optional: public.events.avg_rating (added by ratings migration). If your
-- project hasn't run that migration yet, comment out the avg_rating column +
-- corresponding values in the events INSERT below.
--
-- Idempotent: safe to re-run. Skips rows that already exist by primary key.
--
-- Notes on `created_by`:
--   The events_set_created_by BEFORE INSERT trigger always overrides created_by
--   from auth.uid(). In the SQL editor (postgres role) auth.uid() is NULL, so
--   the seeded events end up with created_by = NULL. They will be visible in
--   the public feed but **cannot** be edited/deleted via app RLS — that's by
--   design for sample/demo data.
--
-- Does NOT insert auth.users / profiles / RSVPs (those need real auth users).
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- Areas (Kampala-focused labels used in the app)
-- -----------------------------------------------------------------------------
-- public.areas has only PRIMARY KEY (id) — no UNIQUE on name — so we conflict
-- on id, not name. (Re-running this script with the same fixed ids is a no-op.)
INSERT INTO public.areas (id, name) VALUES
  ('a0000001-0000-4000-8000-000000000001', 'Bugolobi'),
  ('a0000002-0000-4000-8000-000000000002', 'Kololo'),
  ('a0000003-0000-4000-8000-000000000003', 'Ntinda'),
  ('a0000004-0000-4000-8000-000000000004', 'Wandegeya'),
  ('a0000005-0000-4000-8000-000000000005', 'Kyadondo'),
  ('a0000006-0000-4000-8000-000000000006', 'Entebbe'),
  ('a0000007-0000-4000-8000-000000000007', 'Jinja'),
  ('a0000008-0000-4000-8000-000000000008', 'Mbarara')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Categories (names must match what the Android mapper expects:
-- Music, Food, Comedy, Art, Wellness, Venue — case-insensitive match in app)
-- -----------------------------------------------------------------------------
INSERT INTO public.categories (id, name) VALUES
  ('c0000001-0000-4000-8000-000000000001', 'Music'),
  ('c0000002-0000-4000-8000-000000000002', 'Food'),
  ('c0000003-0000-4000-8000-000000000003', 'Comedy'),
  ('c0000004-0000-4000-8000-000000000004', 'Art'),
  ('c0000005-0000-4000-8000-000000000005', 'Wellness'),
  ('c0000006-0000-4000-8000-000000000006', 'Venue')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Events (fixed ids for stable deep links during dev)
-- -----------------------------------------------------------------------------
-- Schema reminder: public.events.date / start_time / end_time are TEXT.
-- We seed ISO-formatted strings directly so PostgREST returns them as-is.
INSERT INTO public.events (
  id,
  title,
  location,
  is_free,
  description,
  rsvp_count,
  image_url,
  area_id,
  category_id,
  avg_rating,
  price,
  date,
  start_time,
  end_time
) VALUES
  (
    'e0000001-0000-4000-8000-000000000001',
    'Street Food Fest',
    'Bugolobi Market',
    true,
    'Evening street food from vendors across Kampala. Come hungry.',
    124,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Bugolobi' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Food' LIMIT 1),
    4.6,
    NULL,
    to_char(CURRENT_DATE + INTERVAL '3 day', 'YYYY-MM-DD'),
    '17:00',
    '21:00'
  ),
  (
    'e0000002-0000-4000-8000-000000000002',
    'Live Band Night',
    'Kololo Lounge',
    false,
    'Local bands and guest artists. Arrive early for seating.',
    89,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Kololo' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Music' LIMIT 1),
    4.4,
    10000,
    to_char(CURRENT_DATE + INTERVAL '5 day', 'YYYY-MM-DD'),
    '20:00',
    '23:00'
  ),
  (
    'e0000003-0000-4000-8000-000000000003',
    'DJ Party',
    'Ntinda Hub',
    true,
    'Late-night DJ sets and dance floor. Free entry before 10pm.',
    62,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Ntinda' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Music' LIMIT 1),
    3.5,
    NULL,
    to_char(CURRENT_DATE + INTERVAL '6 day', 'YYYY-MM-DD'),
    '21:30',
    '02:00'
  ),
  (
    'e0000004-0000-4000-8000-000000000004',
    'Zumba in the Park',
    'Kyadondo Rugby Club grounds',
    false,
    'Outdoor Zumba for all levels. Bring water and a mat.',
    45,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Kyadondo' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Wellness' LIMIT 1),
    4.8,
    5000,
    to_char(CURRENT_DATE + INTERVAL '1 day', 'YYYY-MM-DD'),
    '08:00',
    '09:30'
  ),
  (
    'e0000005-0000-4000-8000-000000000005',
    'Art Exhibition Opening',
    'Kololo Gallery Row',
    true,
    'Opening night for new contemporary Ugandan artists.',
    12,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Kololo' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Art' LIMIT 1),
    4.2,
    NULL,
    to_char(CURRENT_DATE + INTERVAL '8 day', 'YYYY-MM-DD'),
    '10:00',
    '18:00'
  ),
  (
    'e0000006-0000-4000-8000-000000000006',
    'Comedy Night',
    'Wandegeya Theatre',
    false,
    'Stand-up showcase with headliner from Nairobi.',
    150,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Wandegeya' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Comedy' LIMIT 1),
    4.9,
    20000,
    to_char(CURRENT_DATE + INTERVAL '4 day', 'YYYY-MM-DD'),
    '19:00',
    '22:00'
  ),
  (
    'e0000007-0000-4000-8000-000000000007',
    'Rooftop Sundowner',
    'Kololo Skyline Terrace',
    false,
    'Sunset drinks, small plates, and city views.',
    78,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Kololo' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Venue' LIMIT 1),
    4.3,
    15000,
    to_char(CURRENT_DATE + INTERVAL '2 day', 'YYYY-MM-DD'),
    '17:30',
    '21:00'
  ),
  (
    'e0000008-0000-4000-8000-000000000008',
    'Craft & Design Market',
    'Entebbe Botanical Gardens',
    true,
    'Handmade crafts, jewelry, and live acoustic sets.',
    33,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Entebbe' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Art' LIMIT 1),
    4.1,
    NULL,
    to_char(CURRENT_DATE + INTERVAL '10 day', 'YYYY-MM-DD'),
    '11:00',
    '17:00'
  ),
  (
    'e0000009-0000-4000-8000-000000000009',
    'Nile Jazz Sessions',
    'Jinja Source of the Nile',
    false,
    'Jazz quartet by the river. Limited seating.',
    41,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Jinja' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Music' LIMIT 1),
    4.7,
    12000,
    to_char(CURRENT_DATE + INTERVAL '12 day', 'YYYY-MM-DD'),
    '18:00',
    '21:30'
  ),
  (
    'e000000a-0000-4000-8000-00000000000a',
    'Mbarara Run Club 5K',
    'Mbarara University grounds',
    true,
    'Community fun run — register on site from 7am.',
    210,
    NULL,
    (SELECT id FROM public.areas WHERE name = 'Mbarara' LIMIT 1),
    (SELECT id FROM public.categories WHERE name = 'Wellness' LIMIT 1),
    4.0,
    NULL,
    to_char(CURRENT_DATE + INTERVAL '7 day', 'YYYY-MM-DD'),
    '07:00',
    '09:00'
  )
ON CONFLICT (id) DO NOTHING;

COMMIT;

-- =============================================================================
-- Optional: link a real auth user as the host of a seeded event so the
-- "Hosting" tab in the app shows it. Replace the UUID and event id, then run.
-- =============================================================================
-- BEGIN;
-- UPDATE public.events
--   SET created_by = '<auth.users.id>'
--   WHERE id = 'e0000001-0000-4000-8000-000000000001';
-- COMMIT;
--
-- =============================================================================
-- Optional: after you create a test user in Authentication, link a profile:
-- =============================================================================
-- INSERT INTO public.profiles (id, username, full_name)
-- VALUES ('<auth.users id>', 'devuser', 'Dev User')
-- ON CONFLICT (id) DO NOTHING;
-- =============================================================================
