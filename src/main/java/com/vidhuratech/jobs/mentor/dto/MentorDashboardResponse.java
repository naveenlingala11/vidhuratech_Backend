package com.vidhuratech.jobs.mentor.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class MentorDashboardResponse {
    private MentorProfileResponse profile;
    
    // Core statistics
    private Integer menteesCount;
    private Integer upcomingSessionsCount;
    private Integer completedSessionsCount;
    private Integer pendingFeedbackCount;
    private Integer avgProgress;
    private BigDecimal totalEarnings;
    
    // Detailed list items
    private List<MenteeProgressItem> menteeProgressList;
    private List<UpcomingMeetingItem> upcomingMeetingsList;
    private List<GoalItem> goalsList;

    @Data
    @NoArgsConstructor
    public static class MenteeProgressItem {
        private Long relationId;
        private String name;
        private Integer progress;
        private String avatar;
        private String lastMeetingDate;
        private String targetMilestone;
    }

    @Data
    @NoArgsConstructor
    public static class UpcomingMeetingItem {
        private String mentee;
        private String date;
        private String time;
        private String type;
        private String meetingLink;
    }

    @Data
    @NoArgsConstructor
    public static class GoalItem {
        private String title;
        private String description;
        private Boolean completed;
        private String dueDate;
    }
}
