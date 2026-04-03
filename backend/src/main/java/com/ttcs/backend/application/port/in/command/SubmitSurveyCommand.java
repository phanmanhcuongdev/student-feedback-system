package com.ttcs.backend.application.port.in.command;

import java.util.List;

public record SubmitSurveyCommand(
        Integer surveyId,
        Integer studentId,
        List<SubmitSurveyAnswerCommand> answers
) {
}
