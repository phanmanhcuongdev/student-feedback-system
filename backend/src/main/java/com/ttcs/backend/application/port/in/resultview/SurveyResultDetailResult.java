package com.ttcs.backend.application.port.in.resultview;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyResultDetailResult(
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
        List<QuestionStatisticsResult> questions
) {
}
