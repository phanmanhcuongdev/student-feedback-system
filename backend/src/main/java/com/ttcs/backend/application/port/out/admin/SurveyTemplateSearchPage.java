package com.ttcs.backend.application.port.out.admin;

import com.ttcs.backend.application.domain.model.SurveyTemplate;

import java.util.List;

public record SurveyTemplateSearchPage(
        List<SurveyTemplate> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
