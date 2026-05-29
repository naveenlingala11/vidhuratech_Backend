package com.vidhuratech.jobs.common.notification.repository;

import com.vidhuratech.jobs.common.notification.entity.ActivityNotification;
import com.vidhuratech.jobs.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityNotificationRepository extends JpaRepository<ActivityNotification, Long> {

    @Query("""
        SELECT n FROM ActivityNotification n
        WHERE (n.recipientUser.id = :userId OR n.recipientRole = :role)
        ORDER BY n.createdAt DESC
    """)
    List<ActivityNotification> findForUser(Long userId, UserRole role);

    @Query("""
        SELECT COUNT(n) FROM ActivityNotification n
        WHERE (n.recipientUser.id = :userId OR n.recipientRole = :role)
        AND n.read = false
    """)
    long countUnread(Long userId, UserRole role);
}