package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class Question {

    private final Integer id;
    private final Integer surveyId;
    private final String content;
    private final String contentTranslated;
    private final String sourceLang;
    private final boolean autoTranslated;
    private final String targetLang;
    private final QuestionType type;
    private final Integer questionBankEntryId;

    public Question(Integer id, Integer surveyId, String content, QuestionType type) {
        this(id, surveyId, content, type, null);
    }

    public Question(Integer id, Integer surveyId, String content, QuestionType type, Integer questionBankEntryId) {
        this(id, surveyId, content, null, null, false, null, type, questionBankEntryId);
    }

    public Question(
            Integer id,
            Integer surveyId,
            String content,
            String contentTranslated,
            String sourceLang,
            boolean autoTranslated,
            String targetLang,
            QuestionType type,
            Integer questionBankEntryId
    ) {
        this.id = id;
        this.surveyId = surveyId;
        this.content = content;
        this.contentTranslated = contentTranslated;
        this.sourceLang = sourceLang;
        this.autoTranslated = autoTranslated;
        this.targetLang = targetLang;
        this.type = type;
        this.questionBankEntryId = questionBankEntryId;
    }

    public boolean isRating() {
        return this.type == QuestionType.RATING;
    }

    public boolean isText() {
        return this.type == QuestionType.TEXT;
    }
}
