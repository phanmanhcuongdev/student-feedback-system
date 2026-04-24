package com.ttcs.backend.application.port.in.admin;

public interface PublishSurveyUseCase {
    default SurveyManagementActionResult publishSurvey(Integer surveyId, Integer actorUserId) {
        return publishSurvey(surveyId, actorUserId, null);
    }

    SurveyManagementActionResult publishSurvey(Integer surveyId, Integer actorUserId, String targetLang);
}
