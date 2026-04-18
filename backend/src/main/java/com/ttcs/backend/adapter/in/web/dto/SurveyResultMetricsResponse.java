package com.ttcs.backend.adapter.in.web.dto;

public record SurveyResultMetricsResponse(
        long total,
        long open,
        long closed,
        double averageResponseRate,
        long totalSubmitted,
        long totalResponses
) {
}
