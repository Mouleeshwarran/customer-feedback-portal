
package com.feedback.portal.dto;

import com.feedback.portal.entity.FeedbackStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record ModerateRequest(
    @NotNull FeedbackStatus decision,
    @NotBlank @Size(max = 500) String reason
) {}
