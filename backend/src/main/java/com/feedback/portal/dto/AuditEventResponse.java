
package com.feedback.portal.dto;

import java.time.Instant;

public record AuditEventResponse(
    Long id,
    String actorEmail,
    String action,
    String entityType,
    Long entityId,
    String details,
    Instant createdAt
) {}
