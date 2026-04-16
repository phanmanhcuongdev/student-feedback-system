package com.ttcs.backend.application.port.in.admin;

public record ManagedSurveyMetricsResult(
        long totalSurveys,
        long totalDrafts,
        long totalPublished,
        long totalOpen,
        long totalClosed,
        long totalHidden
) {
}
