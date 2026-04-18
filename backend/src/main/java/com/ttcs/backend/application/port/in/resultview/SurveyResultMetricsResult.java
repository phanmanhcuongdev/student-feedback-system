package com.ttcs.backend.application.port.in.resultview;

public record SurveyResultMetricsResult(
        long total,
        long open,
        long closed,
        double averageResponseRate,
        long totalSubmitted,
        long totalResponses
) {
}
