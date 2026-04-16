package com.ttcs.backend.application.port.in.admin;

public interface GetManagedSurveysUseCase {
    ManagedSurveyPageResult getSurveys(GetManagedSurveysQuery query);
}
