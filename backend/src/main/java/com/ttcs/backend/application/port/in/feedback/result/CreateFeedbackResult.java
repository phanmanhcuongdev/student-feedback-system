package com.ttcs.backend.application.port.in.feedback.result;

public record CreateFeedbackResult(
        boolean success,
        String code,
        String message
) {
    public static CreateFeedbackResult ok() {
        return new CreateFeedbackResult(true, "FEEDBACK_CREATED", "Feedback submitted successfully.");
    }

    public static CreateFeedbackResult fail(String code, String message) {
        return new CreateFeedbackResult(false, code, message);
    }
}
