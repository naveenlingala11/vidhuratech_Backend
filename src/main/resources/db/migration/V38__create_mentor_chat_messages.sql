-- V38: Create mentor_chat_messages table for in-app chat between student and mentor
CREATE TABLE IF NOT EXISTS mentor_chat_messages (
    id BIGSERIAL PRIMARY KEY,
    relation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_relation FOREIGN KEY (relation_id) REFERENCES mentor_student_relations(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);
