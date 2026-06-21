-- V45: Add reactions, question follows, and tag follows

CREATE TABLE IF NOT EXISTS mentor_answer_reactions (
    id BIGSERIAL PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    emoji VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reaction_answer FOREIGN KEY (answer_id) REFERENCES mentor_answers(id) ON DELETE CASCADE,
    CONSTRAINT fk_reaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_answer_user_emoji UNIQUE (answer_id, user_id, emoji)
);

CREATE TABLE IF NOT EXISTS mentor_question_follows (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follow_question FOREIGN KEY (question_id) REFERENCES mentor_questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_question_user_follow UNIQUE (question_id, user_id)
);

CREATE TABLE IF NOT EXISTS mentor_tag_follows (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tag_follow_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_tag_follow UNIQUE (user_id, tag_name)
);
