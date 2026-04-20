package com.ttcs.backend.application.port.in.admin;

public record SurveyTemplateQuestionResult(
        Integer id,
        Integer questionBankEntryId,
        String content,
        String type,
        int displayOrder
) {
}
