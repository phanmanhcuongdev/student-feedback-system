package com.ttcs.backend.application.port.in.auth.result;

public record LoginResult(
        boolean success,
        String code,
        String message,
        Integer userId,
        String role,
        String studentStatus,
        String accessToken
) {
    public static LoginResult ok(Integer userId, String role, String studentStatus, String accessToken) {
        return new LoginResult(true, "LOGIN_SUCCESS", "Dang nhap thanh cong", userId, role, studentStatus, accessToken);
    }

    public static LoginResult fail(String code, String message) {
        return new LoginResult(false, code, message, null, null, null, null);
    }
}
