package com.vidhuratech.jobs.user.repository;

import com.vidhuratech.jobs.user.entity.UserReputationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserReputationLogRepository extends JpaRepository<UserReputationLog, Long> {
    List<UserReputationLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
