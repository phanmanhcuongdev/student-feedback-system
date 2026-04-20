package com.ttcs.backend.application.port.out.admin;

public record AdminAnalyticsReportDepartment(
        Integer departmentId,
        String departmentName,
        long surveyCount,
        long targetedCount,
        long openedCount,
        long submittedCount,
        double responseRate
) {
}
