ALTER TABLE public_challenge_attempts
    ADD COLUMN IF NOT EXISTS participant_name VARCHAR(160),
    ADD COLUMN IF NOT EXISTS participant_email VARCHAR(180),
    ADD COLUMN IF NOT EXISTS participant_phone VARCHAR(30),
    ADD COLUMN IF NOT EXISTS user_id BIGINT,
    ADD COLUMN IF NOT EXISTS total_execution_time_ms BIGINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_public_challenge_attempts_challenge_score
    ON public_challenge_attempts(challenge_id, score DESC, submitted_at ASC);

CREATE TABLE IF NOT EXISTS public_contest_announcements (
                                                            id BIGSERIAL PRIMARY KEY,
                                                            title VARCHAR(220) NOT NULL,
    message TEXT NOT NULL,
    week_start TIMESTAMP NOT NULL,
    week_end TIMESTAMP NOT NULL,
    challenge_id BIGINT,
    winners_json TEXT,
    published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_public_contest_announcements_week
    ON public_contest_announcements(week_start, week_end);