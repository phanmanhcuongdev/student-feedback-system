package com.ttcs.backend.application.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Survey {

    private final Integer id;
    private final String title;
    private final String titleVi;
    private final String titleEn;
    private final String description;
    private final String descriptionVi;
    private final String descriptionEn;
    private final String sourceLang;
    private final boolean autoTranslated;
    private final String modelInfo;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer createdBy;
    private final boolean hidden;
    private final SurveyLifecycleState lifecycleState;

    public Survey(
            Integer id,
            String title,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer createdBy,
            boolean hidden,
            SurveyLifecycleState lifecycleState
    ) {
        this(
                id,
                title,
                null,
                null,
                description,
                null,
                null,
                null,
                false,
                null,
                startDate,
                endDate,
                createdBy,
                hidden,
                lifecycleState
        );
    }

    public Survey(
            Integer id,
            String title,
            String titleVi,
            String titleEn,
            String description,
            String descriptionVi,
            String descriptionEn,
            String sourceLang,
            boolean autoTranslated,
            String modelInfo,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer createdBy,
            boolean hidden,
            SurveyLifecycleState lifecycleState
    ) {
        this.id = id;
        this.title = title;
        this.titleVi = titleVi;
        this.titleEn = titleEn;
        this.description = description;
        this.descriptionVi = descriptionVi;
        this.descriptionEn = descriptionEn;
        this.sourceLang = sourceLang;
        this.autoTranslated = autoTranslated;
        this.modelInfo = modelInfo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.hidden = hidden;
        this.lifecycleState = lifecycleState;
    }

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

        if (endDate != null && !now.isBefore(endDate)) {
            return SurveyStatus.CLOSED;
        }

        return SurveyStatus.OPEN;
    }

    public String translatedTitleFor(String language) {
        return switch (normalizeLanguage(language)) {
            case "vi" -> titleVi;
            case "en" -> titleEn;
            default -> null;
        };
    }

    public String translatedDescriptionFor(String language) {
        return switch (normalizeLanguage(language)) {
            case "vi" -> descriptionVi;
            case "en" -> descriptionEn;
            default -> null;
        };
    }

    public String displayTitle(String language) {
        String translated = translatedTitleFor(language);
        if (translated != null && !translated.isBlank()) {
            return translated;
        }
        return title;
    }

    public String displayDescription(String language) {
        if (description == null || description.isBlank()) {
            return description;
        }
        String translated = translatedDescriptionFor(language);
        if (translated != null && !translated.isBlank()) {
            return translated;
        }
        return description;
    }

    private String normalizeLanguage(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.split(",")[0].trim().split("-")[0].toLowerCase();
    }
}
