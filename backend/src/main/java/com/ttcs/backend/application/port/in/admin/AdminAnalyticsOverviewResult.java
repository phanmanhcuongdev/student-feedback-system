package com.ttcs.backend.application.port.in.admin;

import java.util.List;

public record AdminAnalyticsOverviewResult(
        AdminAnalyticsMetricsResult metrics,
        List<AdminAnalyticsCountResult> lifecycleCounts,
        List<AdminAnalyticsCountResult> runtimeCounts,
        List<AdminAnalyticsDepartmentResult> departmentBreakdown,
        List<AdminAnalyticsAttentionSurveyResult> attentionSurveys
) {
}
