
package com.feedback.portal.dto;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    boolean active
) {}
