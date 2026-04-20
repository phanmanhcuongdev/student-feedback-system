package com.ttcs.backend.application.port.in.admin;

public record AdminAnalyticsDepartmentResult(
        Integer departmentId,
        String departmentName,
        long surveyCount,
        long targetedCount,
        long openedCount,
        long submittedCount,
        double responseRate
) {
}
