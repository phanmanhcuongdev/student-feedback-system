package com.ttcs.backend.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmitSurveyResponse {
    private boolean success;
    private SubmitSurveyResponseCode code;
    private String message;

    public static SubmitSurveyResponse success(String message) {
        return new SubmitSurveyResponse(true, SubmitSurveyResponseCode.SUBMIT_SUCCESS, message);
    }

    public static SubmitSurveyResponse fail(SubmitSurveyResponseCode code, String message) {
        return new SubmitSurveyResponse(false, code, message);
    }
}