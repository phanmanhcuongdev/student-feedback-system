package com.ttcs.backend.application.port.in.auth.result;

public record VerifyEmailResult(
        boolean success,
        String code,
        String message,
        String studentStatus
) {
    public static VerifyEmailResult ok(String studentStatus) {
        return new VerifyEmailResult(
                true,
                "VERIFY_SUCCESS",
                "Xac nhan email thanh cong. Ban da co the dang nhap de tai len minh chung.",
                studentStatus
        );
    }

    public static VerifyEmailResult fail(String code, String message) {
        return new VerifyEmailResult(false, code, message, null);
    }
}
