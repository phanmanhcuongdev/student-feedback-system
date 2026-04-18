package com.ttcs.backend.application.port.in.admin;

import java.util.List;

public record PendingStudentPageResult(
        List<PendingStudentResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
