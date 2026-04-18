package com.ttcs.backend.application.port.out;

import java.time.LocalDate;

public record LoadSurveyResultsQuery(
        String keyword,
        String lifecycleState,
        String runtimeStatus,
        String recipientScope,
        LocalDate startDateFrom,
        LocalDate endDateTo,
        Integer teacherDepartmentId,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
