ALTER TABLE interview_questions
    ADD COLUMN IF NOT EXISTS batch_id BIGINT,
    ADD COLUMN IF NOT EXISTS trainer_id BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS public_visible BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS public_access_level VARCHAR(50) DEFAULT 'LEAD_REQUIRED',
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS published_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS published_by_user_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_interview_questions_batch
    ON interview_questions(batch_id, active);

CREATE INDEX IF NOT EXISTS idx_interview_questions_public
    ON interview_questions(company, role, public_visible, active);

CREATE INDEX IF NOT EXISTS idx_interview_questions_trainer
    ON interview_questions(trainer_id, created_at DESC);