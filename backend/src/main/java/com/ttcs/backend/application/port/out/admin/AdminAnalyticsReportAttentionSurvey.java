package com.ttcs.backend.application.port.out.admin;

public record AdminAnalyticsReportAttentionSurvey(
        Integer id,
        String title,
        String lifecycleState,
        String runtimeStatus,
        String departmentName,
        long targetedCount,
        long openedCount,
        long submittedCount,
        double responseRate
) {
}
