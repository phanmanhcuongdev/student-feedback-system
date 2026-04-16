package com.ttcs.backend.adapter.in.web.dto;

public record CreateSurveyResponse(
        boolean success,
        Integer surveyId,
        String code,
        String message
) {
}
