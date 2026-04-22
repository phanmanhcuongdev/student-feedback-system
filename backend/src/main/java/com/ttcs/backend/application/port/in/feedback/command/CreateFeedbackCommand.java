package com.ttcs.backend.application.port.in.feedback.command;

public record CreateFeedbackCommand(
        Integer studentId,
        String title,
        String content,
        String targetLang
) {
    public CreateFeedbackCommand(Integer studentId, String title, String content) {
        this(studentId, title, content, null);
    }
}
