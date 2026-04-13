package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyManagementDetailResult(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        boolean hidden,
        String recipientScope,
        Integer recipientDepartmentId,
        long responseCount,
        List<SurveyManagementQuestionResult> questions
) {
}
