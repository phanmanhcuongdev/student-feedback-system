package com.ttcs.backend.adapter.in.web.dto;

public record SurveyTemplateQuestionResponse(
        Integer id,
        Integer questionBankEntryId,
        String content,
        String type,
        int displayOrder
) {
}
