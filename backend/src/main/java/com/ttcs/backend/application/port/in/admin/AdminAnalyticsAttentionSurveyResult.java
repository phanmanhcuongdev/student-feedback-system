package com.ttcs.backend.application.port.in.admin;

public record AdminAnalyticsAttentionSurveyResult(
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
