-- V51: Add host_name and host_email columns to mock_interview_requests table
ALTER TABLE mock_interview_requests ADD COLUMN host_name VARCHAR(255);
ALTER TABLE mock_interview_requests ADD COLUMN host_email VARCHAR(255);
