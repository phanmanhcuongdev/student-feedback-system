package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record PendingStudentPageResponse(
        List<PendingStudentResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
