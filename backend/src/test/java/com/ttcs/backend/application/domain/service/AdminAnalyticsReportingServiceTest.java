package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.port.in.admin.AdminAnalyticsOverviewQuery;
import com.ttcs.backend.application.port.in.admin.AdminAnalyticsOverviewResult;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReport;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportAttentionSurvey;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportCount;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportDepartment;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportMetrics;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportQuery;
import com.ttcs.backend.application.port.out.admin.LoadAdminAnalyticsPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminAnalyticsReportingServiceTest {

    @Test
    void shouldLoadAnalyticsOverviewThroughReportingPort() {
        AnalyticsPort port = new AnalyticsPort();
        AdminAnalyticsReportingService service = new AdminAnalyticsReportingService(port);
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        AdminAnalyticsOverviewResult result = service.getOverview(new AdminAnalyticsOverviewQuery(startDate, endDate, 7));

        assertEquals(new AdminAnalyticsReportQuery(startDate, endDate, 7), port.query);
        assertEquals(12, result.metrics().totalSurveys());
        assertEquals("PUBLISHED", result.lifecycleCounts().getFirst().key());
        assertEquals("OPEN", result.runtimeCounts().getFirst().key());
        assertEquals(7, result.departmentBreakdown().getFirst().departmentId());
        assertEquals(99, result.attentionSurveys().getFirst().id());
    }

    @Test
    void shouldAcceptNullQuery() {
        AnalyticsPort port = new AnalyticsPort();
        AdminAnalyticsReportingService service = new AdminAnalyticsReportingService(port);

        service.getOverview(null);

        assertEquals(new AdminAnalyticsReportQuery(null, null, null), port.query);
    }

    private static final class AnalyticsPort implements LoadAdminAnalyticsPort {
        private AdminAnalyticsReportQuery query;

        @Override
        public AdminAnalyticsReport loadOverview(AdminAnalyticsReportQuery query) {
            this.query = query;
            return new AdminAnalyticsReport(
                    new AdminAnalyticsReportMetrics(12, 1, 8, 2, 1, 3, 5, 100, 80, 60, 60.0),
                    List.of(new AdminAnalyticsReportCount("PUBLISHED", 8)),
                    List.of(new AdminAnalyticsReportCount("OPEN", 5)),
                    List.of(new AdminAnalyticsReportDepartment(7, "Engineering", 4, 40, 30, 20, 50.0)),
                    List.of(new AdminAnalyticsReportAttentionSurvey(99, "Course feedback", "PUBLISHED", "OPEN", "Engineering", 30, 20, 10, 33.3))
            );
        }
    }
}
