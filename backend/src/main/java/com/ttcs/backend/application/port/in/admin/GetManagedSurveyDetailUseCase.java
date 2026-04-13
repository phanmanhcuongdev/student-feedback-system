package com.ttcs.backend.application.port.in.admin;

public interface GetManagedSurveyDetailUseCase {
    SurveyManagementDetailResult getSurvey(Integer surveyId);
}
