package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CreateSurveyRequest(
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<CreateQuestionRequest> questions,
        String recipientScope,
        Integer recipientDepartmentId
) {
}
