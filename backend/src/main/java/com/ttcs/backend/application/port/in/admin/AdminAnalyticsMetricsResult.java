package com.ttcs.backend.application.port.in.admin;

public record AdminAnalyticsMetricsResult(
        long totalSurveys,
        long totalDrafts,
        long totalPublished,
        long totalClosed,
        long totalArchived,
        long totalHidden,
        long totalOpenRuntime,
        long totalTargeted,
        long totalOpened,
        long totalSubmitted,
        double averageResponseRate
) {
}
