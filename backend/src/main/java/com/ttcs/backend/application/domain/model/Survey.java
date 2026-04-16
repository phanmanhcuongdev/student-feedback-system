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
    private final boolean hidden;
    private final SurveyLifecycleState lifecycleState;

    public boolean isNotStarted() {
        return status() == SurveyStatus.NOT_OPEN;
    }

    public boolean isClosed() {
        return status() == SurveyStatus.CLOSED;
    }

    public boolean isOpen() {
        return status() == SurveyStatus.OPEN;
    }

    public boolean isDraft() {
        return lifecycleState == SurveyLifecycleState.DRAFT;
    }

    public boolean isPublished() {
        return lifecycleState == SurveyLifecycleState.PUBLISHED;
    }

    public boolean isLifecycleClosed() {
        return lifecycleState == SurveyLifecycleState.CLOSED;
    }

    public boolean isArchived() {
        return lifecycleState == SurveyLifecycleState.ARCHIVED;
    }

    public SurveyStatus status() {
        return statusAt(LocalDateTime.now());
    }

    public SurveyStatus statusAt(LocalDateTime now) {
        if (lifecycleState == SurveyLifecycleState.DRAFT) {
            return SurveyStatus.NOT_OPEN;
        }
        if (lifecycleState == SurveyLifecycleState.CLOSED || lifecycleState == SurveyLifecycleState.ARCHIVED) {
            return SurveyStatus.CLOSED;
        }

        if (startDate != null && now.isBefore(startDate)) {
            return SurveyStatus.NOT_OPEN;
        }

        if (endDate != null && now.isAfter(endDate)) {
            return SurveyStatus.CLOSED;
        }

        return SurveyStatus.OPEN;
    }
}
