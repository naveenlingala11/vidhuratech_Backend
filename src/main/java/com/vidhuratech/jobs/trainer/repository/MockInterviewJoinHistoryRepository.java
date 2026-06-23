package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.MockInterviewJoinHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MockInterviewJoinHistoryRepository extends JpaRepository<MockInterviewJoinHistory, Long> {
    List<MockInterviewJoinHistory> findByMockInterviewIdOrderByJoinedAtDesc(Long mockInterviewId);
}
