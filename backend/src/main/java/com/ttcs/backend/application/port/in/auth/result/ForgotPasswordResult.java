package com.ttcs.backend.application.port.in.auth.result;

public record ForgotPasswordResult(
        boolean success,
        String code,
        String message
) {
    public static ForgotPasswordResult ok() {
        return new ForgotPasswordResult(
                true,
                "RESET_EMAIL_SENT",
                "If the account exists, password reset instructions have been sent to the email address."
        );
    }

    public static ForgotPasswordResult fail(String code, String message) {
        return new ForgotPasswordResult(false, code, message);
    }
}
