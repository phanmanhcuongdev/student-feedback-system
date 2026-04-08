package com.ttcs.backend.application.port.in.resultview;

import java.util.List;

public record QuestionStatisticsResult(
        Integer id,
        String content,
        String type,
        Long responseCount,
        Double averageRating,
        List<RatingBreakdownResult> ratingBreakdown,
        List<String> comments
) {
}
