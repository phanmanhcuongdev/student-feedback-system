package com.ttcs.backend.application.port.out.admin;

import com.ttcs.backend.application.domain.model.SurveyTemplate;

import java.util.Optional;

public interface ManageSurveyTemplatePort {
    SurveyTemplateSearchPage loadPage(SurveyTemplateSearchQuery query);

    Optional<SurveyTemplate> loadById(Integer id);

    SurveyTemplate save(SurveyTemplate template);
}
