package com.ttcs.backend.application.port.out.admin;

import java.util.List;

public record AdminAnalyticsReport(
        AdminAnalyticsReportMetrics metrics,
        List<AdminAnalyticsReportCount> lifecycleCounts,
        List<AdminAnalyticsReportCount> runtimeCounts,
        List<AdminAnalyticsReportDepartment> departmentBreakdown,
        List<AdminAnalyticsReportAttentionSurvey> attentionSurveys
) {
}
