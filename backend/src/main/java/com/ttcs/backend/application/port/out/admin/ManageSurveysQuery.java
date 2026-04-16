package com.ttcs.backend.application.port.out.admin;

import java.time.LocalDate;

public record ManageSurveysQuery(
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
