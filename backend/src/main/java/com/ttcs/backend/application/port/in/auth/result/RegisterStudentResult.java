package com.ttcs.backend.application.port.in.auth.result;

public record RegisterStudentResult(
        boolean success,
        String code,
        String message,
        String verificationUrl
) {
    public static RegisterStudentResult ok(String verificationUrl) {
        return new RegisterStudentResult(
                true,
                "REGISTER_SUCCESS",
                "Dang ky thanh cong. Vui long xac nhan email de tiep tuc.",
                verificationUrl
        );
    }

    public static RegisterStudentResult fail(String code, String message) {
        return new RegisterStudentResult(false, code, message, null);
    }
}
