package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record StudentSurveyPageResponse(
        List<SurveyResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
