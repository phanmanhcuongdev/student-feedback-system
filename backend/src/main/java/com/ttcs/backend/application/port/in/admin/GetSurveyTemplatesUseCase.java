package com.ttcs.backend.application.port.in.admin;

public interface GetSurveyTemplatesUseCase {
    SurveyTemplatePageResult list(GetSurveyTemplatesQuery query);
}
