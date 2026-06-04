CREATE TABLE IF NOT EXISTS public_challenge_discussions (
                                                            id BIGSERIAL PRIMARY KEY,
                                                            challenge_id BIGINT NOT NULL,
                                                            parent_id BIGINT NULL,
                                                            author_name VARCHAR(140),
    author_email VARCHAR(180),
    author_key VARCHAR(140),
    comment TEXT,
    like_count INT DEFAULT 0,
    report_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

ALTER TABLE public_challenge_discussions
    ADD COLUMN IF NOT EXISTS parent_id BIGINT NULL;

ALTER TABLE public_challenge_discussions
    ADD COLUMN IF NOT EXISTS report_count INT DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_public_discussion_challenge
    ON public_challenge_discussions(challenge_id);

CREATE INDEX IF NOT EXISTS idx_public_discussion_parent
    ON public_challenge_discussions(parent_id);

CREATE TABLE IF NOT EXISTS public_challenge_discussion_likes (
                                                                 id BIGSERIAL PRIMARY KEY,
                                                                 discussion_id BIGINT NOT NULL,
                                                                 liker_key VARCHAR(180) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_public_discussion_like_user UNIQUE (discussion_id, liker_key)
    );

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