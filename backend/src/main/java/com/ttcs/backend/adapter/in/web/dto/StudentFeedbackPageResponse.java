package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record StudentFeedbackPageResponse(
        List<StudentFeedbackResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
