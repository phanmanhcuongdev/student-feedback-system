package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SurveyAssignment {
    private final Integer id;
    private final Survey survey;
    private final EvaluatorType evaluatorType;
    private final Integer evaluatorValue;
    private final SubjectType subjectType;
    private final Integer subjectValue;
}