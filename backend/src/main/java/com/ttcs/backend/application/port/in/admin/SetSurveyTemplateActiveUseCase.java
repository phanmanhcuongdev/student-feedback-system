package com.ttcs.backend.application.port.in.admin;

public interface SetSurveyTemplateActiveUseCase {
    SurveyTemplateResult setActive(Integer id, boolean active);
}
