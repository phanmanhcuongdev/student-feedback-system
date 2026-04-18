package com.ttcs.backend.application.port.in.feedback;

import com.ttcs.backend.application.port.in.feedback.result.StudentFeedbackResult;

import java.util.List;

public record StudentFeedbackPageResult(
        List<StudentFeedbackResult> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
