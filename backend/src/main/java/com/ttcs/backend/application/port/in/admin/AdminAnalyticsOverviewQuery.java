package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDate;

public record AdminAnalyticsOverviewQuery(
        LocalDate startDateFrom,
        LocalDate endDateTo,
        Integer departmentId
) {
}
