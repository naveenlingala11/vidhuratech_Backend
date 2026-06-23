-- V53: Retroactively mark sessions created via public meeting links as is_public = true
-- Sessions with meeting links containing 'VidhuraTech_Mock_Session_' or 'VidhuraTech_Meeting_Session_'
-- that have no associated batch are public/quick sessions.
UPDATE mock_interview_requests
SET is_public = TRUE
WHERE (meeting_link LIKE '%VidhuraTech_Mock_Session_%'
   OR meeting_link LIKE '%VidhuraTech_Meeting_Session_%')
  AND batch_id IS NULL;

-- Also mark sessions where there is no student (student_id is NULL) and no batch as public
UPDATE mock_interview_requests
SET is_public = TRUE
WHERE student_id IS NULL
  AND batch_id IS NULL;
