package com.ttcs.backend.adapter.in.web.dto;

public record AdminAnalyticsMetricsResponse(
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
