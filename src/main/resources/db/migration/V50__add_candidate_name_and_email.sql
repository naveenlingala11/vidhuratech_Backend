-- V50: Add candidate_name and candidate_email columns to support unregistered/guest mock sessions
ALTER TABLE mock_interview_requests ADD COLUMN candidate_name VARCHAR(255);
ALTER TABLE mock_interview_requests ADD COLUMN candidate_email VARCHAR(255);
