package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorGoalRepository extends JpaRepository<MentorGoal, Long> {

    List<MentorGoal> findAllByMentorIdOrderByCompletedAscIdDesc(Long mentorId);

    long countByMentorIdAndCompleted(Long mentorId, boolean completed);
}
