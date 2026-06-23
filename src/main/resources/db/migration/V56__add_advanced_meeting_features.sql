-- V56: Add advanced meeting features and join telemetry
ALTER TABLE mock_interview_requests ADD COLUMN join_count INTEGER DEFAULT 0;
ALTER TABLE mock_interview_requests ADD COLUMN recurring_type VARCHAR(50) DEFAULT 'ONCE';
ALTER TABLE mock_interview_requests ADD COLUMN recurring_days VARCHAR(255);
ALTER TABLE mock_interview_requests ADD COLUMN invited_emails TEXT;
ALTER TABLE mock_interview_requests ADD COLUMN preferred_end_time TIME;
ALTER TABLE mock_interview_requests ADD COLUMN timezone VARCHAR(50) DEFAULT 'Asia/Kolkata';

CREATE TABLE mock_interview_join_history (
    id BIGSERIAL PRIMARY KEY,
    mock_interview_id BIGINT REFERENCES mock_interview_requests(id) ON DELETE CASCADE,
    joined_by_name VARCHAR(255),
    joined_by_email VARCHAR(255),
    joined_by_role VARCHAR(50),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
