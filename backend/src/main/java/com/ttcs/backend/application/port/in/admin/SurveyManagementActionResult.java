package com.ttcs.backend.application.port.in.admin;

public record SurveyManagementActionResult(
        boolean success,
        String code,
        String message
) {
    public static SurveyManagementActionResult ok(String code, String message) {
        return new SurveyManagementActionResult(true, code, message);
    }

    public static SurveyManagementActionResult fail(String code, String message) {
        return new SurveyManagementActionResult(false, code, message);
    }
}
