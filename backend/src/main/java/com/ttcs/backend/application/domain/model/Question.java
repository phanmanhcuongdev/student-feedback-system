package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Question {

    private final Integer id;
    private final Integer surveyId;
    private final String content;
    private final QuestionType type;

    public boolean isRating() {
        return this.type == QuestionType.RATING;
    }

    public boolean isText() {
        return this.type == QuestionType.TEXT;
    }
}