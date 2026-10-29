
package com.feedback.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String action;

    @Size(max = 100)
    @Column(length = 100)
    private String entityType;

    private Long entityId;

    @Size(max = 1000)
    @Column(length = 1000)
    private String details;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AuditEvent() {}

    public AuditEvent(User actor, String action, String entityType,
                      Long entityId, String details) {
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public Long getId() { return id; }
    public User getActor() { return actor; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
