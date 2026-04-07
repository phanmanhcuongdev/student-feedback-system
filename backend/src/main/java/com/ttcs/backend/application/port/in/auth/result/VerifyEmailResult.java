package com.ttcs.backend.application.port.in.auth.result;

public record VerifyEmailResult(
        boolean success,
        String code,
        String message,
        Integer studentId,
        String studentStatus
) {
    public static VerifyEmailResult ok(Integer studentId, String studentStatus) {
        return new VerifyEmailResult(true, "VERIFY_SUCCESS", "Xac nhan email thanh cong.", studentId, studentStatus);
    }

    public static VerifyEmailResult fail(String code, String message) {
        return new VerifyEmailResult(false, code, message, null, null);
    }
}
