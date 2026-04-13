package com.ttcs.backend.application.port.out;

public interface LoadSurveyResponsePort {
    boolean existsBySurveyIdAndStudentId(Integer surveyId, Integer studentId);

    long countBySurveyId(Integer surveyId);
}
