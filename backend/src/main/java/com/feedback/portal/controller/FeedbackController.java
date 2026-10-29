
package com.feedback.portal.controller;

import com.feedback.portal.dto.*;
import com.feedback.portal.service.FeedbackService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    public FeedbackResponse submit(
            @Valid @RequestBody FeedbackRequest request,
            Principal principal) {
        return service.submit(principal.getName(), request);
    }

    @GetMapping("/public")
    public Page<FeedbackResponse> publicFeedback(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.publicFeedback(search, categoryId, rating, page, size);
    }

    @GetMapping("/my")
    public Page<FeedbackResponse> myFeedback(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.myFeedback(principal.getName(), page, size);
    }

   @PostMapping("/{feedbackId}/report")
public ResponseEntity<Void> reportFeedback(
        @PathVariable Long feedbackId,
        @Valid @RequestBody AbuseFlagRequest request,
        Principal principal) {

    service.flagFeedback(
            principal.getName(),
            feedbackId,
            request
    );

    return ResponseEntity.noContent().build();
}
}
