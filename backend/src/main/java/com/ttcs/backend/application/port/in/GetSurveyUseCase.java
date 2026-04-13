package com.ttcs.backend.application.port.in;

import com.ttcs.backend.application.port.in.result.SurveySummaryResult;

import java.util.List;

public interface GetSurveyUseCase {
    SurveySummaryResult getSurveyById(Integer surveyId, Integer studentUserId);
    List<SurveySummaryResult> getAllSurveys(Integer studentUserId);
}
