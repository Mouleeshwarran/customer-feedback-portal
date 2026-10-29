
package com.feedback.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String originalFilename;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String contentType;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, unique = true, length = 500)
    private String storageKey;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Attachment() {}

    public Attachment(Feedback feedback, String originalFilename,
                      String contentType, String storageKey, Long fileSize) {
        this.feedback = feedback;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.storageKey = storageKey;
        this.fileSize = fileSize;
    }

    public Long getId() { return id; }
    public Feedback getFeedback() { return feedback; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public String getStorageKey() { return storageKey; }
    public Long getFileSize() { return fileSize; }
    public Instant getCreatedAt() { return createdAt; }
}
