package com.ttcs.backend.application.port.in.resultview;

import java.time.LocalDate;

public record GetSurveyResultsQuery(
        String keyword,
        String lifecycleState,
        String runtimeStatus,
        String recipientScope,
        LocalDate startDateFrom,
        LocalDate endDateTo,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
