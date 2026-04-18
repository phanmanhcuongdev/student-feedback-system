package com.ttcs.backend.application.port.out;

public record SurveyResultMetrics(
        long total,
        long open,
        long closed,
        double averageResponseRate,
        long totalSubmitted,
        long totalResponses
) {
}
