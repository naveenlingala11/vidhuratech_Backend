package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorChatMessageRepository extends JpaRepository<MentorChatMessage, Long> {

    @Query("SELECT m FROM MentorChatMessage m JOIN FETCH m.sender WHERE m.relation.id = :relationId ORDER BY m.createdAt ASC")
    List<MentorChatMessage> findAllByRelationIdOrderByCreatedAtAsc(@Param("relationId") Long relationId);
}
