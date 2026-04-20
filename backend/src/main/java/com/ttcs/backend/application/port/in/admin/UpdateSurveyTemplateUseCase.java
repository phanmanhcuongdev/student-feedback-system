package com.ttcs.backend.application.port.in.admin;

public interface UpdateSurveyTemplateUseCase {
    SurveyTemplateResult update(Integer id, SurveyTemplateCommand command);
}
