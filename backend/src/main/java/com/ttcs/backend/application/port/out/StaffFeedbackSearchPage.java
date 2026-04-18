package com.ttcs.backend.application.port.out;

import java.util.List;

public record StaffFeedbackSearchPage(
        List<StaffFeedbackSearchItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
