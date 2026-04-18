package com.ttcs.backend.application.port.out;

import java.util.List;

public record StudentFeedbackSearchPage(
        List<StudentFeedbackSearchItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
