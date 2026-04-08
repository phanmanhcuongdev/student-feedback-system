package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record QuestionStatisticsResponse(
        Integer id,
        String content,
        String type,
        Long responseCount,
        Double averageRating,
        List<RatingBreakdownResponse> ratingBreakdown,
        List<String> comments
) {
}
