package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record AdminAnalyticsOverviewResponse(
        AdminAnalyticsMetricsResponse metrics,
        List<AdminAnalyticsCountResponse> lifecycleCounts,
        List<AdminAnalyticsCountResponse> runtimeCounts,
        List<AdminAnalyticsDepartmentResponse> departmentBreakdown,
        List<AdminAnalyticsAttentionSurveyResponse> attentionSurveys
) {
}
