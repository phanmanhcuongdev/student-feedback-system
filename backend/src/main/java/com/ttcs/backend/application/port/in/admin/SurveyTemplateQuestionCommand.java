package com.ttcs.backend.application.port.in.admin;

public record SurveyTemplateQuestionCommand(
        Integer questionBankEntryId,
        String content,
        String type
) {
}
