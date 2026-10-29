
package com.feedback.portal.controller;

import com.feedback.portal.dto.*;
import com.feedback.portal.service.FeedbackService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/feedback")
public class ModerationController {

    private final FeedbackService service;

    public ModerationController(FeedbackService service) {
        this.service = service;
    }

    @GetMapping
    public Page<FeedbackResponse> pending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.pendingFeedback(page, size);
    }

    @PatchMapping("/{id}/moderate")
    public FeedbackResponse moderate(
            @PathVariable Long id,
            @Valid @RequestBody ModerateRequest request,
            Principal principal) {
        return service.moderate(principal.getName(), id, request);
    }
}
