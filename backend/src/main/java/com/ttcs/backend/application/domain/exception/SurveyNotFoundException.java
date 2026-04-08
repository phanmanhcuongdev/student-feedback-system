package com.ttcs.backend.application.domain.exception;

public class SurveyNotFoundException extends RuntimeException {

    public SurveyNotFoundException(Integer surveyId) {
        super("Survey " + surveyId + " not found");
    }
}
