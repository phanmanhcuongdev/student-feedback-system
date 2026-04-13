package com.ttcs.backend.application.port.in.admin;

public interface UpdateSurveyUseCase {
    SurveyManagementActionResult updateSurvey(UpdateSurveyCommand command);
}
