package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record SurveyTemplateRequest(
        String name,
        String description,
        String suggestedTitle,
        String suggestedSurveyDescription,
        String recipientScope,
        Integer recipientDepartmentId,
        List<SurveyTemplateQuestionRequest> questions
) {
}
