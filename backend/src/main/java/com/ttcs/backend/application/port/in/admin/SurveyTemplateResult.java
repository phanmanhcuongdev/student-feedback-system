package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyTemplateResult(
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
        List<SurveyTemplateQuestionResult> questions
) {
}
