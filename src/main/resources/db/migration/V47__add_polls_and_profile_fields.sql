-- V47: Add polls and user profile fields for Q&A gamification

CREATE TABLE mentor_polls (
  id BIGSERIAL PRIMARY KEY,
  question_id BIGINT NOT NULL,
  option_text VARCHAR(500) NOT NULL,
  votes_count INT DEFAULT 0,
  CONSTRAINT fk_poll_question FOREIGN KEY (question_id) REFERENCES mentor_questions(id) ON DELETE CASCADE
);

CREATE TABLE mentor_poll_votes (
  id BIGSERIAL PRIMARY KEY,
  poll_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_poll_vote_poll FOREIGN KEY (poll_id) REFERENCES mentor_polls(id) ON DELETE CASCADE,
  CONSTRAINT fk_poll_vote_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE(poll_id, user_id)
);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS bio TEXT,
  ADD COLUMN IF NOT EXISTS skills TEXT,
  ADD COLUMN IF NOT EXISTS social_links TEXT,
  ADD COLUMN IF NOT EXISTS member_since TIMESTAMP;
