
package com.feedback.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "moderation_decisions")
public class ModerationDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moderator_id", nullable = false)
    private User moderator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackStatus decision;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ModerationDecision() {}

    public ModerationDecision(Feedback feedback, User moderator,
                              FeedbackStatus decision, String reason) {
        this.feedback = feedback;
        this.moderator = moderator;
        this.decision = decision;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public Feedback getFeedback() { return feedback; }
    public User getModerator() { return moderator; }
    public FeedbackStatus getDecision() { return decision; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
