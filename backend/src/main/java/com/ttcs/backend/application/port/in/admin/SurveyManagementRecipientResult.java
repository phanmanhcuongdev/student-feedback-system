package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDateTime;

public record SurveyManagementRecipientResult(
        Integer studentId,
        String studentName,
        String studentCode,
        String departmentName,
        String participationStatus,
        LocalDateTime openedAt,
        LocalDateTime submittedAt
) {
}
