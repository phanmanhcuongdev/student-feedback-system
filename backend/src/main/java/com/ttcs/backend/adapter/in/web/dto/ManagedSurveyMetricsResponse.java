package com.ttcs.backend.adapter.in.web.dto;

public record ManagedSurveyMetricsResponse(
        long totalSurveys,
        long totalDrafts,
        long totalPublished,
        long totalOpen,
        long totalClosed,
        long totalHidden
) {
}
