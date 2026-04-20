package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record SurveyTemplatePageResponse(
        List<SurveyTemplateResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
