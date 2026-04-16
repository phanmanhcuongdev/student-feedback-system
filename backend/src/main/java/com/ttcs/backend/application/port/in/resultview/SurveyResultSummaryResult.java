package com.ttcs.backend.application.port.in.resultview;

import java.time.LocalDateTime;

public record SurveyResultSummaryResult(
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
