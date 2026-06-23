-- V52: Add is_public column to mock_interview_requests table
ALTER TABLE mock_interview_requests ADD COLUMN is_public BOOLEAN DEFAULT FALSE;
