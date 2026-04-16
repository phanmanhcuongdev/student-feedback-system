package com.ttcs.backend.application.port.out.admin;

import java.util.List;

public record ManagedUserSearchPage(
        List<ManagedUserSearchItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        ManagedUserMetrics metrics
) {
}
