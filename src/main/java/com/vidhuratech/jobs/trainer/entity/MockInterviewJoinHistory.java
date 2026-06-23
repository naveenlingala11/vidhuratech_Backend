package com.vidhuratech.jobs.trainer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mock_interview_join_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewJoinHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_interview_id", nullable = false)
    private MockInterviewRequest mockInterview;

    private String joinedByName;
    private String joinedByEmail;
    private String joinedByRole;

    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}
