package com.ttcs.backend.adapter.out.persistence.analytics;

import com.ttcs.backend.adapter.out.persistence.reporting.ReportingSqlFragments;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReport;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportAttentionSurvey;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportCount;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportDepartment;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportMetrics;
import com.ttcs.backend.application.port.out.admin.AdminAnalyticsReportQuery;
import com.ttcs.backend.application.port.out.admin.LoadAdminAnalyticsPort;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class AdminAnalyticsPersistenceAdapter implements LoadAdminAnalyticsPort {

    private final EntityManager entityManager;

    private static final String RUNTIME_STATUS_SQL = ReportingSqlFragments.runtimeStatus("s");

    private static final String RESPONSE_RATE_SQL = ReportingSqlFragments.recipientResponseRate("recipient_stats");

    private static final String RUNTIME_STATUS_EXPR = "(" + RUNTIME_STATUS_SQL + ")";

    private static final String BASE_FROM = """
            FROM Survey s
            OUTER APPLY (
                SELECT TOP 1
                    sa.subject_type,
                    sa.subject_value
                FROM Survey_Assignment sa
                WHERE sa.survey_id = s.survey_id
                ORDER BY sa.id ASC
            ) first_assignment
            LEFT JOIN Department d
                ON d.dept_id = first_assignment.subject_value
                AND first_assignment.subject_type = 'DEPARTMENT'
            """ + ReportingSqlFragments.recipientStatsJoin("s", "recipient_stats");

    @Override
    public AdminAnalyticsReport loadOverview(AdminAnalyticsReportQuery query) {
        return new AdminAnalyticsReport(
                loadMetrics(query),
                loadLifecycleCounts(query),
                loadRuntimeCounts(query),
                loadDepartmentBreakdown(query),
                loadAttentionSurveys(query)
        );
    }

    private AdminAnalyticsReportMetrics loadMetrics(AdminAnalyticsReportQuery queryFilter) {
        Query query = entityManager.createNativeQuery("""
                SELECT
                    COUNT(*) AS total_surveys,
                    SUM(CASE WHEN s.lifecycle_state = 'DRAFT' THEN 1 ELSE 0 END) AS total_drafts,
                    SUM(CASE WHEN s.lifecycle_state = 'PUBLISHED' THEN 1 ELSE 0 END) AS total_published,
                    SUM(CASE WHEN s.lifecycle_state = 'CLOSED' THEN 1 ELSE 0 END) AS total_closed,
                    SUM(CASE WHEN s.lifecycle_state = 'ARCHIVED' THEN 1 ELSE 0 END) AS total_archived,
                    SUM(CASE WHEN s.hidden = 1 THEN 1 ELSE 0 END) AS total_hidden,
                    SUM(CASE WHEN """ + RUNTIME_STATUS_EXPR + """
                        = 'OPEN' THEN 1 ELSE 0 END) AS total_open_runtime,
                    SUM(COALESCE(recipient_stats.targeted_count, 0)) AS total_targeted,
                    SUM(COALESCE(recipient_stats.opened_count, 0)) AS total_opened,
                    SUM(COALESCE(recipient_stats.submitted_count, 0)) AS total_submitted,
                    AVG(CAST(""" + RESPONSE_RATE_SQL + """
                        AS FLOAT)) AS average_response_rate
                """ + BASE_FROM + buildWhereClause(queryFilter));
        applyParameters(query, queryFilter);
        Object[] row = (Object[]) query.getSingleResult();
        return new AdminAnalyticsReportMetrics(
                getLong(row[0]),
                getLong(row[1]),
                getLong(row[2]),
                getLong(row[3]),
                getLong(row[4]),
                getLong(row[5]),
                getLong(row[6]),
                getLong(row[7]),
                getLong(row[8]),
                getLong(row[9]),
                getDouble(row[10])
        );
    }

    private List<AdminAnalyticsReportCount> loadLifecycleCounts(AdminAnalyticsReportQuery queryFilter) {
        Query query = entityManager.createNativeQuery("""
                SELECT s.lifecycle_state, COUNT(*)
                """ + BASE_FROM + buildWhereClause(queryFilter) + """
                GROUP BY s.lifecycle_state
                ORDER BY s.lifecycle_state
                """);
        applyParameters(query, queryFilter);
        return rows(query).stream()
                .map(row -> new AdminAnalyticsReportCount(value(row[0]), getLong(row[1])))
                .toList();
    }

    private List<AdminAnalyticsReportCount> loadRuntimeCounts(AdminAnalyticsReportQuery queryFilter) {
        Query query = entityManager.createNativeQuery("""
                SELECT """ + RUNTIME_STATUS_EXPR + """
                    AS runtime_status, COUNT(*)
                """ + BASE_FROM + buildWhereClause(queryFilter) + """
                GROUP BY """ + RUNTIME_STATUS_EXPR + """
                ORDER BY runtime_status
                """);
        applyParameters(query, queryFilter);
        return rows(query).stream()
                .map(row -> new AdminAnalyticsReportCount(value(row[0]), getLong(row[1])))
                .toList();
    }

    private List<AdminAnalyticsReportDepartment> loadDepartmentBreakdown(AdminAnalyticsReportQuery queryFilter) {
        Query query = entityManager.createNativeQuery("""
                SELECT
                    CASE WHEN first_assignment.subject_type = 'DEPARTMENT' THEN d.dept_id ELSE NULL END AS department_id,
                    CASE WHEN first_assignment.subject_type = 'DEPARTMENT' THEN d.name ELSE 'All students' END AS department_name,
                    COUNT(*) AS survey_count,
                    SUM(COALESCE(recipient_stats.targeted_count, 0)) AS targeted_count,
                    SUM(COALESCE(recipient_stats.opened_count, 0)) AS opened_count,
                    SUM(COALESCE(recipient_stats.submitted_count, 0)) AS submitted_count,
                    CASE
                        WHEN SUM(COALESCE(recipient_stats.targeted_count, 0)) = 0 THEN 0
                        ELSE (SUM(COALESCE(recipient_stats.submitted_count, 0)) * 100.0)
                            / SUM(COALESCE(recipient_stats.targeted_count, 0))
                    END AS response_rate
                """ + BASE_FROM + buildWhereClause(queryFilter) + """
                GROUP BY
                    CASE WHEN first_assignment.subject_type = 'DEPARTMENT' THEN d.dept_id ELSE NULL END,
                    CASE WHEN first_assignment.subject_type = 'DEPARTMENT' THEN d.name ELSE 'All students' END
                ORDER BY submitted_count DESC, survey_count DESC
                """);
        applyParameters(query, queryFilter);
        return rows(query).stream()
                .map(row -> new AdminAnalyticsReportDepartment(
                        row[0] == null ? null : ((Number) row[0]).intValue(),
                        value(row[1]),
                        getLong(row[2]),
                        getLong(row[3]),
                        getLong(row[4]),
                        getLong(row[5]),
                        getDouble(row[6])
                ))
                .toList();
    }

    private List<AdminAnalyticsReportAttentionSurvey> loadAttentionSurveys(AdminAnalyticsReportQuery queryFilter) {
        Query query = entityManager.createNativeQuery("""
                SELECT TOP 5
                    s.survey_id,
                    s.title,
                    s.lifecycle_state,
                    """ + RUNTIME_STATUS_EXPR + """
                    AS runtime_status,
                    d.name AS department_name,
                    COALESCE(recipient_stats.targeted_count, 0) AS targeted_count,
                    COALESCE(recipient_stats.opened_count, 0) AS opened_count,
                    COALESCE(recipient_stats.submitted_count, 0) AS submitted_count,
                    """ + RESPONSE_RATE_SQL + """
                    AS response_rate
                """ + BASE_FROM + buildWhereClause(queryFilter, List.of(
                RUNTIME_STATUS_EXPR + " = 'OPEN'",
                "COALESCE(recipient_stats.targeted_count, 0) > 0",
                "COALESCE(recipient_stats.submitted_count, 0) < COALESCE(recipient_stats.targeted_count, 0)"
        )) + """
                ORDER BY response_rate ASC,
                    (COALESCE(recipient_stats.targeted_count, 0) - COALESCE(recipient_stats.submitted_count, 0)) DESC,
                    s.survey_id DESC
                """);
        applyParameters(query, queryFilter);
        return rows(query).stream()
                .map(row -> new AdminAnalyticsReportAttentionSurvey(
                        ((Number) row[0]).intValue(),
                        value(row[1]),
                        value(row[2]),
                        value(row[3]),
                        value(row[4]),
                        getLong(row[5]),
                        getLong(row[6]),
                        getLong(row[7]),
                        getDouble(row[8])
                ))
                .toList();
    }

    private String buildWhereClause(AdminAnalyticsReportQuery query) {
        return buildWhereClause(query, List.of());
    }

    private String buildWhereClause(AdminAnalyticsReportQuery query, List<String> additionalClauses) {
        List<String> clauses = new ArrayList<>();
        if (query != null && query.startDateFrom() != null) {
            clauses.add("s.start_date IS NOT NULL AND s.start_date >= :startDateFrom");
        }
        if (query != null && query.endDateTo() != null) {
            clauses.add("s.end_date IS NOT NULL AND s.end_date < :endDateTo");
        }
        if (query != null && query.departmentId() != null) {
            clauses.add("first_assignment.subject_type = 'DEPARTMENT' AND first_assignment.subject_value = :departmentId");
        }
        clauses.addAll(additionalClauses);
        return clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
    }

    private void applyParameters(Query query, AdminAnalyticsReportQuery filter) {
        if (filter == null) {
            return;
        }
        if (filter.startDateFrom() != null) {
            query.setParameter("startDateFrom", filter.startDateFrom().atStartOfDay());
        }
        if (filter.endDateTo() != null) {
            query.setParameter("endDateTo", filter.endDateTo().plusDays(1).atStartOfDay());
        }
        if (filter.departmentId() != null) {
            query.setParameter("departmentId", filter.departmentId());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> rows(Query query) {
        return query.getResultList();
    }

    private long getLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.longValue();
        }
        return ((Number) value).longValue();
    }

    private double getDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.doubleValue();
        }
        return ((Number) value).doubleValue();
    }

    private String value(Object value) {
        return value == null ? null : value.toString();
    }
}
