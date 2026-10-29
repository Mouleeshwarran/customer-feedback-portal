
package com.feedback.portal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FeedbackRequest(
    @NotNull Long categoryId,
    @NotNull @Min(1) @Max(5) Integer rating,
    @NotBlank @Size(max = 3000) String text,
    boolean privateFeedback
) {}
