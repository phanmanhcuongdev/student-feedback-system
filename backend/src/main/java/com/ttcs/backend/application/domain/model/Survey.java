package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Survey {

    private final Integer id;
    private final String title;
    private final String description;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer createdBy;

    public boolean isNotStarted() {
        return status() == SurveyStatus.NOT_OPEN;
    }

    public boolean isClosed() {
        return status() == SurveyStatus.CLOSED;
    }

    public boolean isOpen() {
        return status() == SurveyStatus.OPEN;
    }

    public SurveyStatus status() {
        return statusAt(LocalDateTime.now());
    }

    public SurveyStatus statusAt(LocalDateTime now) {
        if (startDate != null && now.isBefore(startDate)) {
            return SurveyStatus.NOT_OPEN;
        }

        if (endDate != null && now.isAfter(endDate)) {
            return SurveyStatus.CLOSED;
        }

        return SurveyStatus.OPEN;
    }
}
