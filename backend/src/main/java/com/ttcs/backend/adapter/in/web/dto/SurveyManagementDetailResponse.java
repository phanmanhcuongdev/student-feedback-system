package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyManagementDetailResponse(
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
        List<SurveyManagementQuestionResponse> questions
) {
}
