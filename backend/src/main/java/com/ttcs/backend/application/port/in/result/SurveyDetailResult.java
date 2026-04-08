package com.ttcs.backend.application.port.in.result;

import com.ttcs.backend.application.domain.model.SurveyStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyDetailResult(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        SurveyStatus status,
        List<QuestionItemResult> questions
) {
}
