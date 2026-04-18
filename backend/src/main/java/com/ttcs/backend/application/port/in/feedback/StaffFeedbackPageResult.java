package com.ttcs.backend.application.port.in.feedback;

import com.ttcs.backend.application.port.in.feedback.result.StaffFeedbackResult;

import java.util.List;

public record StaffFeedbackPageResult(
        List<StaffFeedbackResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
