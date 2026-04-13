package com.ttcs.backend.application.port.in.auth.result;

public record ResetPasswordResult(
        boolean success,
        String code,
        String message
) {
    public static ResetPasswordResult ok() {
        return new ResetPasswordResult(
                true,
                "PASSWORD_RESET_SUCCESS",
                "Password updated successfully. You can now sign in with the new password."
        );
    }

    public static ResetPasswordResult fail(String code, String message) {
        return new ResetPasswordResult(false, code, message);
    }
}
