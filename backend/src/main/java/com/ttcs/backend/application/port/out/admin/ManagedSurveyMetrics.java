package com.ttcs.backend.application.port.out.admin;

public record ManagedSurveyMetrics(
        long totalSurveys,
        long totalDrafts,
        long totalPublished,
        long totalOpen,
        long totalClosed,
        long totalHidden
) {
}
