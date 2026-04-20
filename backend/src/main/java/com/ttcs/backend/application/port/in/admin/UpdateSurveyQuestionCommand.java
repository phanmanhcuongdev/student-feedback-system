package com.ttcs.backend.application.port.in.admin;

public record UpdateSurveyQuestionCommand(
        String content,
        String type,
        Integer questionBankEntryId
) {
    public UpdateSurveyQuestionCommand(String content, String type) {
        this(content, type, null);
    }
}
