package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsAttentionSurveyResponse;
import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsCountResponse;
import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsDepartmentResponse;
import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsMetricsResponse;
import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsOverviewResponse;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsAttentionSurveyResult;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsCountResult;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsDepartmentResult;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsMetricsResult;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsOverviewQuery;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsOverviewResult;
import com.ttcs.backend.application.port.in.admin.GetAdminAnalyticsOverviewUseCase;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@WebAdapter
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final GetAdminAnalyticsOverviewUseCase getAdminAnalyticsOverviewUseCase;

    @GetMapping("/overview")
    public ResponseEntity<AdminAnalyticsOverviewResponse> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @RequestParam(required = false) Integer departmentId
    ) {
        AdminAnalyticsOverviewResult result = getAdminAnalyticsOverviewUseCase.getOverview(
                new AdminAnalyticsOverviewQuery(startDateFrom, endDateTo, departmentId)
        );
        return ResponseEntity.ok(toResponse(result));
    }

    private AdminAnalyticsOverviewResponse toResponse(AdminAnalyticsOverviewResult result) {
        return new AdminAnalyticsOverviewResponse(
                toResponse(result.metrics()),
                result.lifecycleCounts().stream().map(this::toResponse).toList(),
                result.runtimeCounts().stream().map(this::toResponse).toList(),
                result.departmentBreakdown().stream().map(this::toResponse).toList(),
                result.attentionSurveys().stream().map(this::toResponse).toList()
        );
    }

    private AdminAnalyticsMetricsResponse toResponse(AdminAnalyticsMetricsResult result) {
        return new AdminAnalyticsMetricsResponse(
                result.totalSurveys(),
                result.totalDrafts(),
                result.totalPublished(),
                result.totalClosed(),
                result.totalArchived(),
                result.totalHidden(),
                result.totalOpenRuntime(),
                result.totalTargeted(),
                result.totalOpened(),
                result.totalSubmitted(),
                result.averageResponseRate()
        );
    }

    private AdminAnalyticsCountResponse toResponse(AdminAnalyticsCountResult result) {
        return new AdminAnalyticsCountResponse(result.key(), result.count());
    }

    private AdminAnalyticsDepartmentResponse toResponse(AdminAnalyticsDepartmentResult result) {
        return new AdminAnalyticsDepartmentResponse(
                result.departmentId(),
                result.departmentName(),
                result.surveyCount(),
                result.targetedCount(),
                result.openedCount(),
                result.submittedCount(),
                result.responseRate()
        );
    }

    private AdminAnalyticsAttentionSurveyResponse toResponse(AdminAnalyticsAttentionSurveyResult result) {
        return new AdminAnalyticsAttentionSurveyResponse(
                result.id(),
                result.title(),
                result.lifecycleState(),
                result.runtimeStatus(),
                result.departmentName(),
                result.targetedCount(),
                result.openedCount(),
                result.submittedCount(),
                result.responseRate()
        );
    }
}
