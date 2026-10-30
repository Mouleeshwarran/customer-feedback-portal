
package com.feedback.portal.dto;

import com.feedback.portal.entity.FeedbackStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerateRequest(
    @NotNull(message = "Decision is required")
    FeedbackStatus decision,

    @NotBlank(message = "A reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reason
) {}
