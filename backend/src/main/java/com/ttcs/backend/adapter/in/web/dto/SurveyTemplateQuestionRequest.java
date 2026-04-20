package com.ttcs.backend.adapter.in.web.dto;

public record SurveyTemplateQuestionRequest(
        Integer questionBankEntryId,
        String content,
        String type
) {
}
