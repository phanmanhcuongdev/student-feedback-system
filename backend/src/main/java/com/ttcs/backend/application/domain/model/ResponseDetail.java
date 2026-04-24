package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDetail {
    private final Integer id;
    private final SurveyResponse response;
    private final Question question;
    private final Integer rating;
    private final String comment;
    private final String commentVi;
    private final String commentEn;
    private final String sourceLang;
    private final boolean autoTranslated;
    private final String modelInfo;

    public ResponseDetail(Integer id, SurveyResponse response, Question question, Integer rating, String comment) {
        this(id, response, question, rating, comment, null, null, null, false, null);
    }
}
