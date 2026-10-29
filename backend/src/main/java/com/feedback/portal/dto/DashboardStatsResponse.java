package com.feedback.portal.dto;

public record DashboardStatsResponse(
    long totalFeedback,
    double averageRating,
    long pendingFeedback
) {}
