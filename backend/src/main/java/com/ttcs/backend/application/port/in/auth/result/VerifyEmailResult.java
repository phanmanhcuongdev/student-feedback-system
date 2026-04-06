package com.ttcs.backend.application.port.in.auth.result;

public record VerifyEmailResult(
        boolean success,
        String code,
        String message
) {
    public static VerifyEmailResult ok() {
        return new VerifyEmailResult(true, "VERIFY_SUCCESS", "Xác nhận email thành công.");
    }

    public static VerifyEmailResult fail(String code, String message) {
        return new VerifyEmailResult(false, code, message);
    }
}
