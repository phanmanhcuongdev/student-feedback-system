package com.ttcs.backend.application.port.in.result;

public record SubmitSurveyResult(
        boolean success,
        SubmitSurveyResultCode code,
        String message
) {
    public static SubmitSurveyResult success(String message) {
        return new SubmitSurveyResult(true, SubmitSurveyResultCode.SUBMIT_SUCCESS, message);
    }

    public static SubmitSurveyResult fail(SubmitSurveyResultCode code, String message) {
        return new SubmitSurveyResult(false, code, message);
    }
}
