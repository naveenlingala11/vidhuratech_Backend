-- V49: Add expiration, duration settings, and granular telemetry logs to mock_interview_requests
ALTER TABLE mock_interview_requests ADD COLUMN expiration_date TIMESTAMP;
ALTER TABLE mock_interview_requests ADD COLUMN max_duration_minutes INTEGER DEFAULT 60;
ALTER TABLE mock_interview_requests ADD COLUMN actual_duration_minutes INTEGER DEFAULT 0;
ALTER TABLE mock_interview_requests ADD COLUMN is_ended BOOLEAN DEFAULT FALSE;
ALTER TABLE mock_interview_requests ADD COLUMN participant_count INTEGER DEFAULT 0;
ALTER TABLE mock_interview_requests ADD COLUMN meeting_logs TEXT;

-- Drop constraints to allow batch-less and student-less quick sessions
ALTER TABLE mock_interview_requests ALTER COLUMN batch_id DROP NOT NULL;
ALTER TABLE mock_interview_requests ALTER COLUMN student_id DROP NOT NULL;
