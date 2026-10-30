
package com.feedback.portal.dto;

import com.feedback.portal.entity.UserRole;
import java.time.Instant;

public record UserResponse(
    Long id,
    String email,
    UserRole role,
    boolean active,
    Instant createdAt
) {}
