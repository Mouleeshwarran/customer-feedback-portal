
package com.feedback.portal.dto;

import com.feedback.portal.entity.FeedbackStatus;
import java.time.Instant;

public record AbuseFlagResponse(
    Long id,
    Long feedbackId,
    String category,
    String feedbackText,
    FeedbackStatus feedbackStatus,
    String reporterEmail,
    String reason,
    Instant createdAt,
    boolean resolved
) {}
