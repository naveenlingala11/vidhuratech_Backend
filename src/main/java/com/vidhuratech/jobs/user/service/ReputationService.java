package com.vidhuratech.jobs.user.service;

import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.entity.UserReputationLog;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.repository.UserReputationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class ReputationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserReputationLogRepository reputationLogRepository;

    @Transactional
    public void awardPoints(Long userId, int points, String reason, String refType, Long refId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Create log entry
        UserReputationLog log = new UserReputationLog();
        log.setUser(user);
        log.setPoints(points);
        log.setReason(reason);
        log.setReferenceType(refType);
        log.setReferenceId(refId);
        reputationLogRepository.save(log);

        // Update user total points
        int newPoints = (user.getReputationPoints() == null ? 0 : user.getReputationPoints()) + points;
        if (newPoints < 0) {
            newPoints = 0;
        }
        user.setReputationPoints(newPoints);

        // Calculate and set level
        user.setReputationLevel(calculateLevel(newPoints));
        userRepository.save(user);
    }

    public String calculateLevel(int points) {
        if (points >= 1000) return "LEGEND";
        if (points >= 500) return "GURU";
        if (points >= 200) return "EXPERT";
        if (points >= 50) return "ACTIVE";
        return "BEGINNER";
    }

    public List<UserReputationLog> getUserLogs(Long userId) {
        return reputationLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Map<String, Object> getUserReputationDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        List<UserReputationLog> logs = getUserLogs(userId);

        List<Map<String, Object>> mappedLogs = logs.stream().map(log -> {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("id", log.getId());
            logMap.put("points", log.getPoints());
            logMap.put("reason", log.getReason());
            logMap.put("referenceType", log.getReferenceType());
            logMap.put("referenceId", log.getReferenceId());
            logMap.put("createdAt", log.getCreatedAt());
            return logMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("userName", user.getName());
        result.put("points", user.getReputationPoints() == null ? 0 : user.getReputationPoints());
        result.put("level", user.getReputationLevel() == null ? "BEGINNER" : user.getReputationLevel());
        result.put("logs", mappedLogs);
        return result;
    }
}
