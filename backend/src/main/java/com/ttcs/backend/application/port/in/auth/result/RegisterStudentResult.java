package com.ttcs.backend.application.port.in.auth.result;

public record RegisterStudentResult(
        boolean success,
        String code,
        String message
) {
    public static RegisterStudentResult ok() {
        return new RegisterStudentResult(true, "REGISTER_SUCCESS", "Đăng ký thành công. Vui lòng kiểm tra email để xác nhận.");
    }

    public static RegisterStudentResult fail(String code, String message) {
        return new RegisterStudentResult(false, code, message);
    }
}