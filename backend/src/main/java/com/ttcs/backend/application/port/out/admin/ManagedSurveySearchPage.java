package com.ttcs.backend.application.port.out.admin;

import java.util.List;

public record ManagedSurveySearchPage(
        List<ManagedSurveySearchItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        ManagedSurveyMetrics metrics
) {
}
