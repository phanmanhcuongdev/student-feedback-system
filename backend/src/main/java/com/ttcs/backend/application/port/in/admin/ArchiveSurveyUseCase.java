package com.ttcs.backend.application.port.in.admin;

public interface ArchiveSurveyUseCase {
    SurveyManagementActionResult archiveSurvey(Integer surveyId, Integer actorUserId);
}
