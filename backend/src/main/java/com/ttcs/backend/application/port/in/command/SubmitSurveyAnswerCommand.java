package com.ttcs.backend.application.port.in.command;

public record SubmitSurveyAnswerCommand(
        Integer questionId,
        Integer rating,
        String comment
) {
}
