package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.port.in.admin.AdminAnalyticsAttentionSurveyResult;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsCountResult;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsDepartmentResult;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsMetricsResult;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsOverviewQuery;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsOverviewResult;
import com.ttcs.backend.application.port.in.admin.GetAdminAnalyticsOverviewUseCase;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReport;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportAttentionSurvey;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportCount;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportDepartment;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportMetrics;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportQuery;
import com.ttcs.backend.application.port.out.admin.LoadAdminAnalyticsPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class AdminAnalyticsReportingService implements GetAdminAnalyticsOverviewUseCase {

    private final LoadAdminAnalyticsPort loadAdminAnalyticsPort;

    @Override
    @Transactional(readOnly = true)
    public AdminAnalyticsOverviewResult getOverview(AdminAnalyticsOverviewQuery query) {
        AdminAnalyticsReport report = loadAdminAnalyticsPort.loadOverview(new AdminAnalyticsReportQuery(
                query == null ? null : query.startDateFrom(),
                query == null ? null : query.endDateTo(),
                query == null ? null : query.departmentId()
        ));

        return new AdminAnalyticsOverviewResult(
                toResult(report.metrics()),
                report.lifecycleCounts().stream().map(this::toResult).toList(),
                report.runtimeCounts().stream().map(this::toResult).toList(),
                report.departmentBreakdown().stream().map(this::toResult).toList(),
                report.attentionSurveys().stream().map(this::toResult).toList()
        );
    }

    private AdminAnalyticsMetricsResult toResult(AdminAnalyticsReportMetrics metrics) {
        return new AdminAnalyticsMetricsResult(
                metrics.totalSurveys(),
                metrics.totalDrafts(),
                metrics.totalPublished(),
                metrics.totalClosed(),
                metrics.totalArchived(),
                metrics.totalHidden(),
                metrics.totalOpenRuntime(),
                metrics.totalTargeted(),
                metrics.totalOpened(),
                metrics.totalSubmitted(),
                metrics.averageResponseRate()
        );
    }

    private AdminAnalyticsCountResult toResult(AdminAnalyticsReportCount count) {
        return new AdminAnalyticsCountResult(count.key(), count.count());
    }

    private AdminAnalyticsDepartmentResult toResult(AdminAnalyticsReportDepartment department) {
        return new AdminAnalyticsDepartmentResult(
                department.departmentId(),
                department.departmentName(),
                department.surveyCount(),
                department.targetedCount(),
                department.openedCount(),
                department.submittedCount(),
                department.responseRate()
        );
    }

    private AdminAnalyticsAttentionSurveyResult toResult(AdminAnalyticsReportAttentionSurvey survey) {
        return new AdminAnalyticsAttentionSurveyResult(
                survey.id(),
                survey.title(),
                survey.lifecycleState(),
                survey.runtimeStatus(),
                survey.departmentName(),
                survey.targetedCount(),
                survey.openedCount(),
                survey.submittedCount(),
                survey.responseRate()
        );
    }
}
