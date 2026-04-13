package com.ttcs.backend.application.port.in.feedback.command;

public record RespondToFeedbackCommand(
        Integer feedbackId,
        Integer responderUserId,
        String content
) {
}
