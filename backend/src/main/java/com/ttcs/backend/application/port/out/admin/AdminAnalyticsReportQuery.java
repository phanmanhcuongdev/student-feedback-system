package com.ttcs.backend.application.port.out.admin;

import java.time.LocalDate;

public record AdminAnalyticsReportQuery(
        LocalDate startDateFrom,
        LocalDate endDateTo,
        Integer departmentId
) {
}
