-- V55: Drop NOT NULL constraints on batch_id and student_id to allow public sessions
ALTER TABLE mock_interview_requests ALTER COLUMN batch_id DROP NOT NULL;
ALTER TABLE mock_interview_requests ALTER COLUMN student_id DROP NOT NULL;
