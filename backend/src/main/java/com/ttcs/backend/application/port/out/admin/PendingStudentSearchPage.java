package com.ttcs.backend.application.port.out.admin;

import java.util.List;

public record PendingStudentSearchPage(
        List<PendingStudentSearchItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
