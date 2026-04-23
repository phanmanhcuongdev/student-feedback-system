package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class Question {

    private final Integer id;
    private final Integer surveyId;
    private final String content;
    private final String contentVi;
    private final String contentEn;
    private final String sourceLang;
    private final boolean autoTranslated;
    private final String modelInfo;
    private final QuestionType type;
    private final Integer questionBankEntryId;

    public Question(Integer id, Integer surveyId, String content, QuestionType type) {
        this(id, surveyId, content, type, null);
    }

    public Question(Integer id, Integer surveyId, String content, QuestionType type, Integer questionBankEntryId) {
        this(id, surveyId, content, null, null, null, false, null, type, questionBankEntryId);
    }

    public Question(
            Integer id,
            Integer surveyId,
            String content,
            String contentVi,
            String contentEn,
            String sourceLang,
            boolean autoTranslated,
            String modelInfo,
            QuestionType type,
            Integer questionBankEntryId
    ) {
        this.id = id;
        this.surveyId = surveyId;
        this.content = content;
        this.contentVi = contentVi;
        this.contentEn = contentEn;
        this.sourceLang = sourceLang;
        this.autoTranslated = autoTranslated;
        this.modelInfo = modelInfo;
        this.type = type;
        this.questionBankEntryId = questionBankEntryId;
    }

    public boolean isRating() {
        return this.type == QuestionType.RATING;
    }

    public boolean isText() {
        return this.type == QuestionType.TEXT;
    }

    public String translatedContentFor(String language) {
        String normalized = normalizeLanguage(language);
        return switch (normalized) {
            case "vi" -> contentVi;
            case "en" -> contentEn;
            default -> null;
        };
    }

    private String normalizeLanguage(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value.split(",")[0].trim().split("-")[0].toLowerCase();
    }
}
