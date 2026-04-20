package com.ttcs.backend.application.port.out;

public record SurveyReportRatingBreakdown(
        Integer rating,
        long count
) {
}
