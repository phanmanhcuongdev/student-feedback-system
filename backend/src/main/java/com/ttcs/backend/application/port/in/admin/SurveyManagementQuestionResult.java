package com.ttcs.backend.application.port.in.admin;

public record SurveyManagementQuestionResult(
        Integer id,
        String content,
        String type
) {
}
