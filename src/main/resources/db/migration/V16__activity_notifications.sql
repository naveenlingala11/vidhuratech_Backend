CREATE TABLE IF NOT EXISTS activity_notifications (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      recipient_user_id BIGINT NULL REFERENCES users(id),
    recipient_role VARCHAR(40) NULL,
    title VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    activity_type VARCHAR(80) NOT NULL,
    link VARCHAR(255),
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_activity_notifications_user
    ON activity_notifications(recipient_user_id, read_flag, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_activity_notifications_role
    ON activity_notifications(recipient_role, read_flag, created_at DESC);