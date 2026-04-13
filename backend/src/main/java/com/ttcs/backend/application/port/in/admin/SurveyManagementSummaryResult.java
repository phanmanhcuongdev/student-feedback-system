package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDateTime;

public record SurveyManagementSummaryResult(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        boolean hidden,
        String recipientScope,
        Integer recipientDepartmentId,
        long responseCount
) {
}
