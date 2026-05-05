package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public record SurveyReminderCandidate(
        Integer userId,
        Integer surveyId,
        String surveyTitle,
        LocalDateTime deadline
) {
}
