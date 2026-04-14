package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyResultDetailResponse(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        Long responseCount,
        Long targetedCount,
        Long openedCount,
        Long submittedCount,
        Double responseRate,
        List<QuestionStatisticsResponse> questions
) {
}
