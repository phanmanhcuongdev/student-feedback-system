package com.ttcs.backend.adapter.in.web.dto;

public record AdminAnalyticsAttentionSurveyResponse(
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
