package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SurveyRecipient {
    private final Integer id;
    private final Integer surveyId;
    private final Integer studentId;
    private final LocalDateTime assignedAt;
    private final LocalDateTime openedAt;
    private final LocalDateTime submittedAt;

    public SurveyRecipientStatus status() {
        if (submittedAt != null) {
            return SurveyRecipientStatus.SUBMITTED;
        }
        if (openedAt != null) {
            return SurveyRecipientStatus.OPENED;
        }
        return SurveyRecipientStatus.ASSIGNED;
    }

    public boolean hasOpened() {
        return openedAt != null;
    }

    public boolean hasSubmitted() {
        return submittedAt != null;
    }
}
