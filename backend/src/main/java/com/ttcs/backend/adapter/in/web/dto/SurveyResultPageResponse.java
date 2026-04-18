package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record SurveyResultPageResponse(
        List<SurveyResultSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        SurveyResultMetricsResponse metrics
) {
}
