
package com.feedback.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AbuseFlagRequest(
    @NotBlank(message = "A reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reason
) {}
