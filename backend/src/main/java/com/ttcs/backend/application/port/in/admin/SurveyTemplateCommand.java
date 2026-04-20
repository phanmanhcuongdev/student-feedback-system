package com.ttcs.backend.application.port.in.admin;

import java.util.List;

public record SurveyTemplateCommand(
        String name,
        String description,
        String suggestedTitle,
        String suggestedSurveyDescription,
        String recipientScope,
        Integer recipientDepartmentId,
        List<SurveyTemplateQuestionCommand> questions
) {
}
