package com.ttcs.backend.application.domain.model;

public record SurveyTemplateQuestion(
        Integer id,
        Integer questionBankEntryId,
        String content,
        String type,
        int displayOrder
) {
}
