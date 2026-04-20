package com.ttcs.backend.application.port.in.resultview;

public record SurveyReportRatingBreakdownView(
        Integer rating,
        long count
) {
}
