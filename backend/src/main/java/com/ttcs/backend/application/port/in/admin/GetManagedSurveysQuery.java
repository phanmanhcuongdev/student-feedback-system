package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDate;

public record GetManagedSurveysQuery(
        String keyword,
        String lifecycleState,
        String runtimeStatus,
        Boolean hidden,
        String recipientScope,
        LocalDate startDateFrom,
        LocalDate endDateTo,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
