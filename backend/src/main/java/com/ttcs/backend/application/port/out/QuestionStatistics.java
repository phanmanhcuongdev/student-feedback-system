package com.ttcs.backend.application.port.out;

import java.util.List;

public record QuestionStatistics(
        Integer id,
        String content,
        String type,
        Long responseCount,
        Double averageRating,
        List<RatingBreakdown> ratingBreakdown,
        List<String> comments
) {
}
