
package com.feedback.portal.service;

import com.feedback.portal.dto.AttachmentResponse;
import com.feedback.portal.entity.Attachment;
import com.feedback.portal.entity.AuditEvent;
import com.feedback.portal.entity.Feedback;
import com.feedback.portal.entity.User;
import com.feedback.portal.repository.AttachmentRepository;
import com.feedback.portal.repository.AuditEventRepository;
import com.feedback.portal.repository.FeedbackRepository;
import com.feedback.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AttachmentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "pdf", "txt", "docx", "xlsx"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif",
        "application/pdf", "text/plain",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    @Value("${app.attachment.storage-dir:./uploads}")
    private String storageDir;

    private final AttachmentRepository attachmentRepo;
    private final FeedbackRepository feedbackRepo;
    private final UserRepository userRepo;
    private final AuditEventRepository auditRepo;

    public AttachmentService(
            AttachmentRepository attachmentRepo,
            FeedbackRepository feedbackRepo,
            UserRepository userRepo,
            AuditEventRepository auditRepo) {
        this.attachmentRepo = attachmentRepo;
        this.feedbackRepo = feedbackRepo;
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
    }

    @Transactional
    public AttachmentResponse upload(String email, Long feedbackId, MultipartFile file) {
        User user = userRepo.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Feedback feedback = feedbackRepo.findById(feedbackId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found"));

        // Ownership check — only the submitter can attach files
        if (!feedback.getCustomer().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You can only attach files to your own feedback");
        }

        // File size check
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "File size exceeds the maximum allowed limit of 10 MB");
        }

        // Extension check (prevent path traversal and executable types)
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String extension = getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        // MIME type check
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "MIME type not allowed: " + contentType);
        }

        // Generate a safe server-side storage key (UUID-based)
        String storageKey = UUID.randomUUID() + "." + extension;

        // Store the file outside the web root
        try {
            Path dir = Paths.get(storageDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path targetPath = dir.resolve(storageKey).normalize();

            // Prevent path traversal: ensure the resolved path is inside the storage dir
            if (!targetPath.startsWith(dir)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file path detected");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to store attachment");
        }

        Attachment attachment = attachmentRepo.save(
            new Attachment(feedback, originalFilename, contentType, storageKey, file.getSize())
        );

        auditRepo.save(new AuditEvent(user, "ATTACHMENT_UPLOADED", "Attachment",
            attachment.getId(), "File: " + originalFilename + " for Feedback#" + feedbackId));

        return toResponse(attachment);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listForFeedback(String email, Long feedbackId) {
        Feedback feedback = feedbackRepo.findById(feedbackId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found"));

        User user = userRepo.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Only the owner or a moderator/admin can list attachments
        boolean isOwner = feedback.getCustomer().getId().equals(user.getId());
        boolean isStaff = user.getRole() != com.feedback.portal.entity.UserRole.CUSTOMER;
        if (!isOwner && !isStaff) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Access to attachments is restricted");
        }

        return attachmentRepo.findByFeedbackId(feedbackId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public java.io.File download(String email, Long attachmentId) {
        Attachment att = attachmentRepo.findById(attachmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        User user = userRepo.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Feedback feedback = att.getFeedback();
        boolean isOwner = feedback.getCustomer().getId().equals(user.getId());
        boolean isStaff = user.getRole() != com.feedback.portal.entity.UserRole.CUSTOMER;
        if (!isOwner && !isStaff) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        Path filePath = Paths.get(storageDir).toAbsolutePath().normalize()
            .resolve(att.getStorageKey()).normalize();

        java.io.File file = filePath.toFile();
        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on disk");
        }
        return file;
    }

    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepo.findById(attachmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));
    }

    private AttachmentResponse toResponse(Attachment a) {
        return new AttachmentResponse(
            a.getId(), a.getFeedback().getId(), a.getOriginalFilename(),
            a.getContentType(), a.getFileSize(), a.getCreatedAt());
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "upload";
        // Strip path separators and null bytes
        return Paths.get(filename).getFileName().toString()
            .replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1)
            ? filename.substring(dot + 1)
            : "";
    }
}
