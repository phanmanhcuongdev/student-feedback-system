package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;

public record SurveyManagementSummaryResponse(
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
