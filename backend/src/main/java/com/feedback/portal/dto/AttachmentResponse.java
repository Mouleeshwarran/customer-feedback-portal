
package com.feedback.portal.dto;

import java.time.Instant;

public record AttachmentResponse(
    Long id,
    Long feedbackId,
    String originalFilename,
    String contentType,
    Long fileSize,
    Instant createdAt
) {}
