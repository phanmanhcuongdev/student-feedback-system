package com.ttcs.backend.application.port.out.admin;

public record AdminAnalyticsReportMetrics(
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
