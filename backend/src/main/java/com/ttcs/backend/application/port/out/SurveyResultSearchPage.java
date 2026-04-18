package com.ttcs.backend.application.port.out;

import java.util.List;

public record SurveyResultSearchPage(
        List<SurveyResultSearchItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        SurveyResultMetrics metrics
) {
}
