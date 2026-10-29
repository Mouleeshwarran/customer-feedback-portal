
package com.feedback.portal.controller;

import com.feedback.portal.dto.AbuseFlagResponse;
import com.feedback.portal.service.FeedbackService;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
public class AbuseReportController {

    private final FeedbackService feedbackService;

    public AbuseReportController(
            FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public Page<AbuseFlagResponse> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return feedbackService.getUnresolvedAbuseFlags(
            page, size
        );
    }

    @PatchMapping("/{id}/resolve")
    public void resolveReport(
            @PathVariable Long id,
            Principal principal) {

        feedbackService.resolveAbuseFlag(
            principal.getName(), id
        );
    }
}
