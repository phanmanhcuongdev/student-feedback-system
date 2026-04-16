package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;

public record SurveyManagementRecipientResponse(
        Integer studentId,
        String studentName,
        String studentCode,
        String departmentName,
        String participationStatus,
        LocalDateTime openedAt,
        LocalDateTime submittedAt
) {
}
