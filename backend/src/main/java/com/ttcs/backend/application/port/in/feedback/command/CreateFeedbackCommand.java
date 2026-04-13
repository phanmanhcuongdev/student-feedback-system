package com.ttcs.backend.application.port.in.feedback.command;

public record CreateFeedbackCommand(
        Integer studentId,
        String title,
        String content
) {
}
