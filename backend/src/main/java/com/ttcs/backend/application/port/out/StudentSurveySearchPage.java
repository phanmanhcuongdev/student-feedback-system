package com.ttcs.backend.application.port.out;

import java.util.List;

public record StudentSurveySearchPage(
        List<StudentSurveySearchItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
