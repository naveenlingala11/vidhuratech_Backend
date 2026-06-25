-- =====================================================
-- V57: Performance indexes for production query speed
-- =====================================================

-- JOBS table: location-based cleanup and filtering
CREATE INDEX IF NOT EXISTS idx_jobs_location ON jobs (location);
CREATE INDEX IF NOT EXISTS idx_jobs_company_id ON jobs (company_id);
CREATE INDEX IF NOT EXISTS idx_jobs_posted_at ON jobs (posted_at);
CREATE INDEX IF NOT EXISTS idx_jobs_created_at ON jobs (created_at);

-- ASSESSMENTS: public practice library listing
CREATE INDEX IF NOT EXISTS idx_assessments_active_public ON assessments (active, public_visible);
CREATE INDEX IF NOT EXISTS idx_assessments_published_at ON assessments (published_at DESC NULLS LAST);

-- PSEUDO_CODE_CHALLENGE: public practice listing
CREATE INDEX IF NOT EXISTS idx_pseudo_code_challenge_active_public ON pseudo_code_challenge (active, public_visible);
CREATE INDEX IF NOT EXISTS idx_pseudo_code_challenge_published_at ON pseudo_code_challenge (published_at DESC NULLS LAST);
CREATE INDEX IF NOT EXISTS idx_pseudo_code_challenge_batch_id ON pseudo_code_challenge (batch_id);

-- INTERVIEW_QUESTIONS: public library listing
CREATE INDEX IF NOT EXISTS idx_interview_questions_active_public ON interview_questions (active, public_visible);
CREATE INDEX IF NOT EXISTS idx_interview_questions_published_at ON interview_questions (published_at DESC NULLS LAST);

-- SCRAPER_CONFIGS: startup load
CREATE INDEX IF NOT EXISTS idx_scraper_configs_active ON scraper_configs (active);
