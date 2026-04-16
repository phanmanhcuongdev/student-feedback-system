package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;

public record SurveyResultSummaryResponse(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        String lifecycleState,
        String runtimeStatus,
        String recipientScope,
        String recipientDepartmentName,
        Long responseCount,
        Long targetedCount,
        Long openedCount,
        Long submittedCount,
        Double responseRate
) {
}
