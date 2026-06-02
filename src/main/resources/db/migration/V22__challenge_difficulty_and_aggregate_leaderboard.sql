ALTER TABLE pseudo_code_challenge
    ADD COLUMN IF NOT EXISTS difficulty_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

CREATE INDEX IF NOT EXISTS idx_public_challenge_attempts_period
    ON public_challenge_attempts(submitted_at, challenge_id, score DESC);