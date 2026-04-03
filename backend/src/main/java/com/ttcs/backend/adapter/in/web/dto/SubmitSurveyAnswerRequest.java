package com.ttcs.backend.adapter.in.web.dto;

public record SubmitSurveyAnswerRequest(
        Integer questionId,
        Integer rating,
        String comment
) {
}
