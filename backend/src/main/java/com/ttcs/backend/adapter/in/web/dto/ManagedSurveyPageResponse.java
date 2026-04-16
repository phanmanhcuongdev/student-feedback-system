package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record ManagedSurveyPageResponse(
        List<SurveyManagementSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        ManagedSurveyMetricsResponse metrics
) {
}
