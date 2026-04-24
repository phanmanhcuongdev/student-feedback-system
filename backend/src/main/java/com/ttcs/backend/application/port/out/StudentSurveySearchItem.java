package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.SurveyStatus;

import java.time.LocalDateTime;

public record StudentSurveySearchItem(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer createdBy,
        SurveyStatus status,
        boolean submitted
) {
}
