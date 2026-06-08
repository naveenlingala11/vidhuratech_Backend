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

CREATE INDEX IF NOT EXISTS idx_public_discussion_challenge
    ON public_challenge_discussions(challenge_id);

CREATE TABLE IF NOT EXISTS public_challenge_discussion_likes (
                                                                 id BIGSERIAL PRIMARY KEY,
                                                                 discussion_id BIGINT NOT NULL,
                                                                 liker_key VARCHAR(180) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_public_discussion_like_user UNIQUE (discussion_id, liker_key)
    );