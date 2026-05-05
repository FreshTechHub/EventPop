-- Event ratings: per-user scores, aggregates on events, RLS, RPC helpers.

CREATE TABLE IF NOT EXISTS public.event_ratings (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  event_id   uuid NOT NULL REFERENCES public.events(id)
             ON DELETE CASCADE,
  user_id    uuid NOT NULL REFERENCES auth.users(id)
             ON DELETE CASCADE,
  score      smallint NOT NULL CHECK (score BETWEEN 1 AND 5),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT event_ratings_event_user_unique
    UNIQUE (event_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_event_ratings_event_id
  ON public.event_ratings (event_id);

CREATE INDEX IF NOT EXISTS idx_event_ratings_user_id
  ON public.event_ratings (user_id);

-- Repair existing tables created before this migration shape.
ALTER TABLE public.event_ratings
  ADD COLUMN IF NOT EXISTS created_at timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now();

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'event_ratings_event_user_unique'
  ) THEN
    ALTER TABLE public.event_ratings
      ADD CONSTRAINT event_ratings_event_user_unique
      UNIQUE (event_id, user_id);
  END IF;
END;
$$;

ALTER TABLE public.events
  ADD COLUMN IF NOT EXISTS avg_rating    numeric(3,2) DEFAULT 0.0,
  ADD COLUMN IF NOT EXISTS rating_count  integer      DEFAULT 0;

CREATE OR REPLACE FUNCTION public.refresh_event_rating()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  UPDATE public.events
  SET
    avg_rating   = (
      SELECT COALESCE(ROUND(AVG(score)::numeric, 2), 0.0)
      FROM   public.event_ratings
      WHERE  event_id = COALESCE(NEW.event_id, OLD.event_id)
    ),
    rating_count = (
      SELECT COUNT(*)::integer
      FROM   public.event_ratings
      WHERE  event_id = COALESCE(NEW.event_id, OLD.event_id)
    )
  WHERE id = COALESCE(NEW.event_id, OLD.event_id);
  RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS trg_refresh_event_rating
  ON public.event_ratings;

CREATE TRIGGER trg_refresh_event_rating
  AFTER INSERT OR UPDATE OR DELETE
  ON public.event_ratings
  FOR EACH ROW
  EXECUTE FUNCTION public.refresh_event_rating();

UPDATE public.events e
SET
  avg_rating   = COALESCE(sub.avg, 0.0),
  rating_count = COALESCE(sub.cnt, 0)
FROM (
  SELECT
    event_id,
    ROUND(AVG(score)::numeric, 2) AS avg,
    COUNT(*)::integer AS cnt
  FROM public.event_ratings
  GROUP BY event_id
) sub
WHERE e.id = sub.event_id;

ALTER TABLE public.event_ratings ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "ratings_select" ON public.event_ratings;
DROP POLICY IF EXISTS "ratings_insert" ON public.event_ratings;
DROP POLICY IF EXISTS "ratings_update" ON public.event_ratings;
DROP POLICY IF EXISTS "ratings_delete" ON public.event_ratings;

CREATE POLICY "ratings_select" ON public.event_ratings
  FOR SELECT USING (auth.role() = 'authenticated');

CREATE POLICY "ratings_insert" ON public.event_ratings
  FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "ratings_update" ON public.event_ratings
  FOR UPDATE USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "ratings_delete" ON public.event_ratings
  FOR DELETE USING (auth.uid() = user_id);

CREATE OR REPLACE FUNCTION public.get_my_rating(p_event_id uuid)
RETURNS TABLE (score smallint)
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT er.score
  FROM   public.event_ratings er
  WHERE  er.event_id = p_event_id
  AND    er.user_id  = auth.uid()
  LIMIT  1;
$$;

CREATE OR REPLACE FUNCTION public.upsert_rating(
  p_event_id uuid,
  p_score    smallint
) RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  IF p_score < 1 OR p_score > 5 THEN
    RAISE EXCEPTION 'score must be 1–5';
  END IF;

  INSERT INTO public.event_ratings (event_id, user_id, score)
  VALUES (p_event_id, auth.uid(), p_score)
  ON CONFLICT (event_id, user_id)
  DO UPDATE SET score = EXCLUDED.score,
                updated_at = now();
END;
$$;

CREATE OR REPLACE FUNCTION public.delete_my_rating(
  p_event_id uuid
) RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  DELETE FROM public.event_ratings
  WHERE event_id = p_event_id
  AND   user_id  = auth.uid();
END;
$$;

GRANT EXECUTE ON FUNCTION public.get_my_rating(uuid) TO authenticated;
GRANT EXECUTE ON FUNCTION public.upsert_rating(uuid, smallint) TO authenticated;
GRANT EXECUTE ON FUNCTION public.delete_my_rating(uuid) TO authenticated;
