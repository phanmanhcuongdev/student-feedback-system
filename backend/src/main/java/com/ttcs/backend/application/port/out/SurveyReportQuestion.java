package com.ttcs.backend.application.port.out;

import java.util.List;

public record SurveyReportQuestion(
        Integer id,
        String content,
        String type,
        long responseCount,
        Double averageRating,
        List<SurveyReportRatingBreakdown> ratingBreakdown,
        List<String> comments
) {
}
