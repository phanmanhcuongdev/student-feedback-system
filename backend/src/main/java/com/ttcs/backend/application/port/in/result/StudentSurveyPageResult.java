package com.ttcs.backend.application.port.in.result;

import java.util.List;

public record StudentSurveyPageResult(
        List<SurveySummaryResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
