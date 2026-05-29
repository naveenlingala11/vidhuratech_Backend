ALTER TABLE assessments
    ADD COLUMN IF NOT EXISTS company_name VARCHAR(120),
    ADD COLUMN IF NOT EXISTS skill VARCHAR(120),
    ADD COLUMN IF NOT EXISTS public_visible BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS public_access_level VARCHAR(40) NOT NULL DEFAULT 'LEAD_REQUIRED',
    ADD COLUMN IF NOT EXISTS public_attempt_limit INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS published_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS published_by_user_id BIGINT;

ALTER TABLE pseudo_code_challenge
    ADD COLUMN IF NOT EXISTS skill VARCHAR(120),
    ADD COLUMN IF NOT EXISTS public_visible BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS public_access_level VARCHAR(40) NOT NULL DEFAULT 'LEAD_REQUIRED',
    ADD COLUMN IF NOT EXISTS public_attempt_limit INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS published_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS published_by_user_id BIGINT;

CREATE TABLE IF NOT EXISTS public_practice_access_grants (
                                                             id BIGSERIAL PRIMARY KEY,
                                                             lead_id BIGINT NOT NULL,
                                                             practice_type VARCHAR(30) NOT NULL,
    practice_id BIGINT NOT NULL,
    access_level VARCHAR(40) NOT NULL,
    access_token VARCHAR(500) NOT NULL UNIQUE,
    max_attempts INTEGER NOT NULL DEFAULT 1,
    attempts_used INTEGER NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_access_grant_lead FOREIGN KEY (lead_id) REFERENCES leads(id)
    );

CREATE INDEX IF NOT EXISTS idx_public_access_token
    ON public_practice_access_grants(access_token);

CREATE INDEX IF NOT EXISTS idx_public_access_practice
    ON public_practice_access_grants(practice_type, practice_id);

CREATE TABLE IF NOT EXISTS public_assessment_attempts (
                                                          id BIGSERIAL PRIMARY KEY,
                                                          access_grant_id BIGINT NOT NULL,
                                                          lead_id BIGINT NOT NULL,
                                                          assessment_id BIGINT NOT NULL,
                                                          score INTEGER NOT NULL DEFAULT 0,
                                                          total_marks INTEGER NOT NULL DEFAULT 0,
                                                          percentage INTEGER NOT NULL DEFAULT 0,
                                                          correct_answers INTEGER NOT NULL DEFAULT 0,
                                                          total_questions INTEGER NOT NULL DEFAULT 0,
                                                          status VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_public_assessment_grant FOREIGN KEY (access_grant_id) REFERENCES public_practice_access_grants(id),
    CONSTRAINT fk_public_assessment_lead FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_public_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(id)
    );

CREATE TABLE IF NOT EXISTS public_challenge_attempts (
                                                         id BIGSERIAL PRIMARY KEY,
                                                         access_grant_id BIGINT NOT NULL,
                                                         lead_id BIGINT NOT NULL,
                                                         challenge_id BIGINT NOT NULL,
                                                         language VARCHAR(40),
    source_code TEXT,
    score INTEGER NOT NULL DEFAULT 0,
    total_marks INTEGER NOT NULL DEFAULT 0,
    percentage INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_public_challenge_grant FOREIGN KEY (access_grant_id) REFERENCES public_practice_access_grants(id),
    CONSTRAINT fk_public_challenge_lead FOREIGN KEY (lead_id) REFERENCES leads(id)
    );