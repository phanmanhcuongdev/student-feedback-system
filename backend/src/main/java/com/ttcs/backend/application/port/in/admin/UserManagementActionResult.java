package com.ttcs.backend.application.port.in.admin;

public record UserManagementActionResult(
        boolean success,
        String code,
        String message
) {
    public static UserManagementActionResult ok(String code, String message) {
        return new UserManagementActionResult(true, code, message);
    }

    public static UserManagementActionResult fail(String code, String message) {
        return new UserManagementActionResult(false, code, message);
    }
}
