package com.ttcs.backend.application.port.in.admin;

public interface CreateSurveyTemplateUseCase {
    SurveyTemplateResult create(SurveyTemplateCommand command);
}
