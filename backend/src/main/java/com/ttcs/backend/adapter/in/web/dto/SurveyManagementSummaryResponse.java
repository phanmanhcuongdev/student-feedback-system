package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;

public record SurveyManagementSummaryResponse(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String lifecycleState,
        String runtimeStatus,
        boolean hidden,
        String recipientScope,
        Integer recipientDepartmentId,
        String recipientDepartmentName,
        long responseCount,
        long targetedCount,
        long openedCount,
        long submittedCount,
        double responseRate
) {
}
