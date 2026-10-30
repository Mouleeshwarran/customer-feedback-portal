
package com.feedback.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank @Size(max = 60) String name,
    @Size(max = 300) String description,
    boolean active
) {}
