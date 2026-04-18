package com.ttcs.backend.application.port.in.resultview;

import java.util.List;

public record SurveyResultPageResult(
        List<SurveyResultSummaryResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        SurveyResultMetricsResult metrics
) {
}
