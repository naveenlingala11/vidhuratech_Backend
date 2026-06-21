package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorPoll;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MentorPollRepository extends JpaRepository<MentorPoll, Long> {
    List<MentorPoll> findAllByQuestionId(Long questionId);
}
