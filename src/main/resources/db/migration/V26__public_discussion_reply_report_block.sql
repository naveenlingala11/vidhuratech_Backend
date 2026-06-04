ALTER TABLE public_challenge_discussions
    ADD COLUMN IF NOT EXISTS parent_id BIGINT NULL;

ALTER TABLE public_challenge_discussions
    ADD COLUMN IF NOT EXISTS report_count INT DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_public_discussion_parent
    ON public_challenge_discussions(parent_id);

CREATE TABLE IF NOT EXISTS public_challenge_discussion_reports (
                                                                   id BIGSERIAL PRIMARY KEY,
                                                                   discussion_id BIGINT NOT NULL,
                                                                   reporter_key VARCHAR(180) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_public_discussion_report_user UNIQUE (discussion_id, reporter_key)
    );

CREATE TABLE IF NOT EXISTS public_challenge_discussion_blocks (
                                                                  id BIGSERIAL PRIMARY KEY,
                                                                  blocker_key VARCHAR(180) NOT NULL,
    blocked_author_key VARCHAR(180) NOT NULL,
    blocked_author_name VARCHAR(180),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_public_discussion_block_user UNIQUE (blocker_key, blocked_author_key)
    );