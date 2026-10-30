
package com.feedback.portal.dto;

import com.feedback.portal.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @Email @NotBlank String email,
    @Size(min = 12, max = 100) String password,
    @NotNull UserRole role,
    boolean active
) {}
