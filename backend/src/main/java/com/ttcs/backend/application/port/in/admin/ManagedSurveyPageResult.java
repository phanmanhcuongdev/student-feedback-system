package com.ttcs.backend.application.port.in.admin;

import java.util.List;

public record ManagedSurveyPageResult(
        List<SurveyManagementSummaryResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        ManagedSurveyMetricsResult metrics
) {
}
