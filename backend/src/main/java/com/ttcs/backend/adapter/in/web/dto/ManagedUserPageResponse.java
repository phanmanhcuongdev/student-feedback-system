package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record ManagedUserPageResponse(
        List<ManagedUserSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        ManagedUserMetricsResponse metrics
) {
}
