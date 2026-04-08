package com.ttcs.backend.application.port.in.resultview;

import java.time.LocalDateTime;

public record SurveyResultSummaryResult(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        Long responseCount
) {
}
