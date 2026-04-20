package com.ttcs.backend.application.port.in.resultview;

import java.util.List;

public record SurveyReportQuestionView(
        Integer id,
        String content,
        String type,
        long responseCount,
        Double averageRating,
        List<SurveyReportRatingBreakdownView> ratingBreakdown,
        List<String> comments
) {
}
