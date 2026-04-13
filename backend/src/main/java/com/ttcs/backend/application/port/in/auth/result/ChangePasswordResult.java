package com.ttcs.backend.application.port.in.auth.result;

public record ChangePasswordResult(
        boolean success,
        String code,
        String message
) {
    public static ChangePasswordResult ok() {
        return new ChangePasswordResult(
                true,
                "PASSWORD_CHANGED",
                "Password changed successfully."
        );
    }

    public static ChangePasswordResult fail(String code, String message) {
        return new ChangePasswordResult(false, code, message);
    }
}
