package com.ttcs.backend.application.port.in.admin;

import java.util.List;

public record ManagedUserPageResult(
        List<ManagedUserSummaryResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        ManagedUserMetricsResult metrics
) {
}
