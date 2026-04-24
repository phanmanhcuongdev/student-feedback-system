package com.ttcs.backend.application.port.in;

import com.ttcs.backend.application.port.in.result.StudentSurveyPageResult;
import com.ttcs.backend.application.port.in.result.SurveySummaryResult;

public interface GetSurveyUseCase {
    SurveySummaryResult getSurveyById(Integer surveyId, Integer studentUserId, String targetLang);
    StudentSurveyPageResult getAllSurveys(GetStudentSurveysQuery query, Integer studentUserId, String targetLang);
}
