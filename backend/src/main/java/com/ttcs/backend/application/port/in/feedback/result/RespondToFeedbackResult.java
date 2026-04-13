package com.ttcs.backend.application.port.in.feedback.result;

public record RespondToFeedbackResult(
        boolean success,
        String code,
        String message
) {
    public static RespondToFeedbackResult ok() {
        return new RespondToFeedbackResult(true, "FEEDBACK_RESPONSE_CREATED", "Response submitted successfully.");
    }

    public static RespondToFeedbackResult fail(String code, String message) {
        return new RespondToFeedbackResult(false, code, message);
    }
}
