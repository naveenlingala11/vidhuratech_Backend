package com.vidhuratech.jobs.mentor.entity;

import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_answer_reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"answer_id", "user_id", "emoji"})
})
@Getter
@Setter
public class MentorAnswerReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private MentorAnswer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String emoji;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
