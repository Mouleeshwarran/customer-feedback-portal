
package com.feedback.portal.controller;

import com.feedback.portal.dto.DashboardStatsResponse;
import com.feedback.portal.service.FeedbackService;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class DashboardController {

    private final FeedbackService feedbackService;

    public DashboardController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/stats")
    public DashboardStatsResponse getDashboardStats(
            Principal principal) {

        return feedbackService.getDashboardStats(
            principal.getName()
        );
    }
}
