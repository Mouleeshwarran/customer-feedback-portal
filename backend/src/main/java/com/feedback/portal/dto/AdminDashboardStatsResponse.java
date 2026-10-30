
package com.feedback.portal.dto;

public record AdminDashboardStatsResponse(
    long totalUsers,
    long totalFeedback,
    double averageRating,
    long pendingModeration,
    long unresolvedAbuseReports,
    long totalCategories
) {}
