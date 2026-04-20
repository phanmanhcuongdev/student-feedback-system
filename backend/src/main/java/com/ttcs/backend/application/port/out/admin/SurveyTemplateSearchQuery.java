package com.ttcs.backend.application.port.out.admin;

public record SurveyTemplateSearchQuery(
        String keyword,
        Boolean active,
        int page,
        int size
) {
}
