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
}