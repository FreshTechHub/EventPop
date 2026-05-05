-- =============================================================================
-- EventPop — development sample data (Supabase SQL Editor or `psql`)
-- =============================================================================
-- Run against your **project** database (Dashboard → SQL → New query).
--
-- Expects tables: public.areas, public.categories, public.events
-- (FKs: events.area_id → areas.id, events.category_id → categories.id)
--
-- Idempotent: safe to re-run. Skips rows that already exist by primary key
-- or unique name.
--
-- Does NOT insert auth.users / profiles / RSVPs (those need real auth users).
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- Areas (Kampala-focused labels used in the app)
-- -----------------------------------------------------------------------------
INSERT INTO public.areas (id, name) VALUES
  ('a0000001-0000-4000-8000-000000000001', 'Bugolobi'),
  ('a0000002-0000-4000-8000-000000000002', 'Kololo'),
  ('a0000003-0000-4000-8000-000000000003', 'Ntinda'),
  ('a0000004-0000-4000-8000-000000000004', 'Wandegeya'),
  ('a0000005-0000-4000-8000-000000000005', 'Kyadondo'),
  ('a0000006-0000-4000-8000-000000000006', 'Entebbe'),
  ('a0000007-0000-4000-8000-000000000007', 'Jinja'),
  ('a0000008-0000-4000-8000-000000000008', 'Mbarara')
ON CONFLICT (name) DO NOTHING;

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
ON CONFLICT (name) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Events (fixed ids for stable deep links during dev)
-- -----------------------------------------------------------------------------
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
  organizer_id,
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
    NULL,
    4.6,
    NULL,
    (CURRENT_DATE + INTERVAL '3 day')::date,
    '17:00'::time,
    '21:00'::time
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
    NULL,
    4.4,
    10000,
    (CURRENT_DATE + INTERVAL '5 day')::date,
    '20:00'::time,
    '23:00'::time
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
    NULL,
    3.5,
    NULL,
    (CURRENT_DATE + INTERVAL '6 day')::date,
    '21:30'::time,
    '02:00'::time
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
    NULL,
    4.8,
    5000,
    (CURRENT_DATE + INTERVAL '1 day')::date,
    '08:00'::time,
    '09:30'::time
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
    NULL,
    4.2,
    NULL,
    (CURRENT_DATE + INTERVAL '8 day')::date,
    '10:00'::time,
    '18:00'::time
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
    NULL,
    4.9,
    20000,
    (CURRENT_DATE + INTERVAL '4 day')::date,
    '19:00'::time,
    '22:00'::time
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
    NULL,
    4.3,
    15000,
    (CURRENT_DATE + INTERVAL '2 day')::date,
    '17:30'::time,
    '21:00'::time
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
    NULL,
    4.1,
    NULL,
    (CURRENT_DATE + INTERVAL '10 day')::date,
    '11:00'::time,
    '17:00'::time
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
    NULL,
    4.7,
    12000,
    (CURRENT_DATE + INTERVAL '12 day')::date,
    '18:00'::time,
    '21:30'::time
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
    NULL,
    4.0,
    NULL,
    (CURRENT_DATE + INTERVAL '7 day')::date,
    '07:00'::time,
    '09:00'::time
  )
ON CONFLICT (id) DO NOTHING;

COMMIT;

-- =============================================================================
-- Optional: after you create a test user in Authentication, link a profile:
-- =============================================================================
-- INSERT INTO public.profiles (id, username, full_name)
-- VALUES ('<auth.users id>', 'devuser', 'Dev User')
-- ON CONFLICT (id) DO NOTHING;
--
-- Then you can set organizer_id on events or seed event_rsvps / event_interests.
-- =============================================================================
