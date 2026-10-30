
package com.feedback.portal.controller;

import com.feedback.portal.dto.AttachmentResponse;
import com.feedback.portal.entity.Attachment;
import com.feedback.portal.service.AttachmentService;
import java.io.File;
import java.security.Principal;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/feedback/{feedbackId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable Long feedbackId,
            @RequestPart("file") MultipartFile file,
            Principal principal) {
        AttachmentResponse response = attachmentService.upload(
            principal.getName(), feedbackId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<AttachmentResponse> list(
            @PathVariable Long feedbackId,
            Principal principal) {
        return attachmentService.listForFeedback(principal.getName(), feedbackId);
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long feedbackId,
            @PathVariable Long attachmentId,
            Principal principal) {
        File file = attachmentService.download(principal.getName(), attachmentId);
        Attachment att = attachmentService.getAttachment(attachmentId);

        // Use the stored MIME type — never execute
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(att.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
            ContentDisposition.attachment()
                .filename(att.getOriginalFilename())
                .build()
        );

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(mediaType)
            .body(new FileSystemResource(file));
    }
}
