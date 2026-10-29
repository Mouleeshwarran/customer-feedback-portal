
package com.feedback.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "abuse_flags")
public class AbuseFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private boolean resolved = false;

    public AbuseFlag() {}

    public AbuseFlag(Feedback feedback, User reporter, String reason) {
        this.feedback = feedback;
        this.reporter = reporter;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public Feedback getFeedback() { return feedback; }
    public User getReporter() { return reporter; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isResolved() { return resolved; }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}
