package com.ttcs.backend.application.port.in.admin;

public record UpdateSurveyQuestionCommand(
        String content,
        String type
) {
}
