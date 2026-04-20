package com.ttcs.backend.application.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyTemplate(
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
        List<SurveyTemplateQuestion> questions
) {
}
