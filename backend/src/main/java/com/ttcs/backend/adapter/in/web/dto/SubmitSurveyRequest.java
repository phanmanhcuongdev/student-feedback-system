package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record SubmitSurveyRequest(
        List<SubmitSurveyAnswerRequest> answers
) {
}
