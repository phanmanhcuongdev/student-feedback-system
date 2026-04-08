package com.ttcs.backend.application.port.in.auth.result;

public record RegisterStudentResult(
        boolean success,
        String code,
        String message
) {
    public static RegisterStudentResult ok() {
        return new RegisterStudentResult(
                true,
                "REGISTER_SUCCESS",
                "Dang ky thanh cong. He thong da gui email xac minh, vui long kiem tra hop thu de tiep tuc."
        );
    }

    public static RegisterStudentResult fail(String code, String message) {
        return new RegisterStudentResult(false, code, message);
    }
}
