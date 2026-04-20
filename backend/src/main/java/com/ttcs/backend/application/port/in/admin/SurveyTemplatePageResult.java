package com.ttcs.backend.application.port.in.admin;

import java.util.List;

public record SurveyTemplatePageResult(
        List<SurveyTemplateResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
