-- V54: Add host_role column to mock_interview_requests table
ALTER TABLE mock_interview_requests ADD COLUMN host_role VARCHAR(50);
