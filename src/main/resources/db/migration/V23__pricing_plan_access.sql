ALTER TABLE invoice
    ADD COLUMN IF NOT EXISTS purchase_type VARCHAR(60),
    ADD COLUMN IF NOT EXISTS plan_code VARCHAR(60);

CREATE TABLE IF NOT EXISTS plan_access_grants (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  user_id BIGINT,
                                                  invoice_id VARCHAR(80) NOT NULL,
    plan_code VARCHAR(60) NOT NULL,
    plan_name VARCHAR(120) NOT NULL,
    buyer_name VARCHAR(160) NOT NULL,
    buyer_email VARCHAR(180) NOT NULL,
    buyer_phone VARCHAR(30) NOT NULL,
    amount DOUBLE PRECISION NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    access_courses BOOLEAN NOT NULL DEFAULT FALSE,
    access_mock_tests BOOLEAN NOT NULL DEFAULT FALSE,
    access_interviews BOOLEAN NOT NULL DEFAULT FALSE,
    access_notes BOOLEAN NOT NULL DEFAULT FALSE,
    access_materials BOOLEAN NOT NULL DEFAULT FALSE,
    access_videos BOOLEAN NOT NULL DEFAULT FALSE,
    access_live_classes BOOLEAN NOT NULL DEFAULT FALSE,
    access_practice_companies BOOLEAN NOT NULL DEFAULT FALSE,
    access_premium_challenges BOOLEAN NOT NULL DEFAULT FALSE,
    company_limit INTEGER NOT NULL DEFAULT 0,
    starts_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_plan_access_user_status
    ON plan_access_grants(user_id, status);

CREATE INDEX IF NOT EXISTS idx_plan_access_email_status
    ON plan_access_grants(lower(buyer_email), status);

CREATE INDEX IF NOT EXISTS idx_plan_access_invoice
    ON plan_access_grants(invoice_id);