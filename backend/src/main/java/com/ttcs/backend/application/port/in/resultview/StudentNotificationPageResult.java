package com.ttcs.backend.application.port.in.resultview;

import java.util.List;

public record StudentNotificationPageResult(
        List<StudentNotificationResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
