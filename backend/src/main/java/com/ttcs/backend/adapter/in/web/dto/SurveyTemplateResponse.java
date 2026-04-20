package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyTemplateResponse(
        Integer id,
        String name,
        String description,
        String suggestedTitle,
        String suggestedSurveyDescription,
        String recipientScope,
        Integer recipientDepartmentId,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SurveyTemplateQuestionResponse> questions
) {
}
