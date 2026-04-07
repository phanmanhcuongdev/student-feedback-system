package com.ttcs.backend.application.port.in.admin;

public record ApprovalActionResult(
        boolean success,
        String code,
        String message
) {
    public static ApprovalActionResult success(String code, String message) {
        return new ApprovalActionResult(true, code, message);
    }

    public static ApprovalActionResult fail(String code, String message) {
        return new ApprovalActionResult(false, code, message);
    }
}
