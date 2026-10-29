
package com.feedback.portal.dto;

import com.feedback.portal.entity.FeedbackStatus;
import java.time.Instant;

public record FeedbackResponse(
    Long id,
    String category,
    Integer rating,
    String text,
    FeedbackStatus status,
    boolean privateFeedback,
    Instant createdAt
) {}
