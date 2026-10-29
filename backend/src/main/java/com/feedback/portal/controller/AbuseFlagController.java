
package com.feedback.portal.controller;

import com.feedback.portal.dto.AbuseFlagRequest;
import com.feedback.portal.service.FeedbackService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class AbuseFlagController {

    private final FeedbackService feedbackService;

    public AbuseFlagController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/{id}/flags")
    public ResponseEntity<Map<String, String>> flagFeedback(
            @PathVariable Long id,
            @Valid @RequestBody AbuseFlagRequest request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Authentication is required"));
        }

        feedbackService.flagFeedback(principal.getName(), id, request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "Feedback reported successfully"));
    }
}
