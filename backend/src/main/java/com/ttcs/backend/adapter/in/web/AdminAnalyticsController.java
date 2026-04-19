package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsAttentionSurveyResponse;
import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsCountResponse;
import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsDepartmentResponse;
import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsMetricsResponse;
import com.ttcs.backend.adapter.in.web.dto.AdminAnalyticsOverviewResponse;
import com.ttcs.backend.common.WebAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebAdapter
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final EntityManager entityManager;

    private static final String RUNTIME_STATUS_SQL = """
            CASE
                WHEN s.lifecycle_state = 'DRAFT' THEN 'NOT_OPEN'
                WHEN s.lifecycle_state IN ('CLOSED', 'ARCHIVED') THEN 'CLOSED'
                WHEN s.start_date IS NOT NULL AND s.start_date > GETDATE() THEN 'NOT_OPEN'
                WHEN s.end_date IS NOT NULL AND s.end_date < GETDATE() THEN 'CLOSED'
                ELSE 'OPEN'
            END
            """;

    private static final String RESPONSE_RATE_SQL = """
            CASE
                WHEN COALESCE(recipient_stats.targeted_count, 0) = 0 THEN 0
                ELSE (COALESCE(recipient_stats.submitted_count, 0) * 100.0) / recipient_stats.targeted_count
            END
            """;

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
            LEFT JOIN (
                SELECT
                    sr.survey_id,
                    COUNT(*) AS targeted_count,
                    SUM(CASE WHEN sr.opened_at IS NOT NULL THEN 1 ELSE 0 END) AS opened_count,
                    SUM(CASE WHEN sr.submitted_at IS NOT NULL THEN 1 ELSE 0 END) AS submitted_count
                FROM Survey_Recipient sr
                GROUP BY sr.survey_id
            ) recipient_stats ON recipient_stats.survey_id = s.survey_id
            """;

    @GetMapping("/overview")
    public ResponseEntity<AdminAnalyticsOverviewResponse> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @RequestParam(required = false) Integer departmentId
    ) {
        AnalyticsFilter filter = new AnalyticsFilter(startDateFrom, endDateTo, departmentId);
        return ResponseEntity.ok(new AdminAnalyticsOverviewResponse(
                loadMetrics(filter),
                loadLifecycleCounts(filter),
                loadRuntimeCounts(filter),
                loadDepartmentBreakdown(filter),
                loadAttentionSurveys(filter)
        ));
    }

    private AdminAnalyticsMetricsResponse loadMetrics(AnalyticsFilter filter) {
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
                """ + BASE_FROM + buildWhereClause(filter));
        applyParameters(query, filter);
        Object[] row = (Object[]) query.getSingleResult();
        return new AdminAnalyticsMetricsResponse(
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

    private List<AdminAnalyticsCountResponse> loadLifecycleCounts(AnalyticsFilter filter) {
        Query query = entityManager.createNativeQuery("""
                SELECT s.lifecycle_state, COUNT(*)
                """ + BASE_FROM + buildWhereClause(filter) + """
                GROUP BY s.lifecycle_state
                ORDER BY s.lifecycle_state
                """);
        applyParameters(query, filter);
        return rows(query).stream()
                .map(row -> new AdminAnalyticsCountResponse(value(row[0]), getLong(row[1])))
                .toList();
    }

    private List<AdminAnalyticsCountResponse> loadRuntimeCounts(AnalyticsFilter filter) {
        Query query = entityManager.createNativeQuery("""
                SELECT """ + RUNTIME_STATUS_EXPR + """
                    AS runtime_status, COUNT(*)
                """ + BASE_FROM + buildWhereClause(filter) + """
                GROUP BY """ + RUNTIME_STATUS_EXPR + """
                ORDER BY runtime_status
                """);
        applyParameters(query, filter);
        return rows(query).stream()
                .map(row -> new AdminAnalyticsCountResponse(value(row[0]), getLong(row[1])))
                .toList();
    }

    private List<AdminAnalyticsDepartmentResponse> loadDepartmentBreakdown(AnalyticsFilter filter) {
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
                """ + BASE_FROM + buildWhereClause(filter) + """
                GROUP BY
                    CASE WHEN first_assignment.subject_type = 'DEPARTMENT' THEN d.dept_id ELSE NULL END,
                    CASE WHEN first_assignment.subject_type = 'DEPARTMENT' THEN d.name ELSE 'All students' END
                ORDER BY submitted_count DESC, survey_count DESC
                """);
        applyParameters(query, filter);
        return rows(query).stream()
                .map(row -> new AdminAnalyticsDepartmentResponse(
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

    private List<AdminAnalyticsAttentionSurveyResponse> loadAttentionSurveys(AnalyticsFilter filter) {
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
                """ + BASE_FROM + buildWhereClause(filter, List.of(
                RUNTIME_STATUS_EXPR + " = 'OPEN'",
                "COALESCE(recipient_stats.targeted_count, 0) > 0",
                "COALESCE(recipient_stats.submitted_count, 0) < COALESCE(recipient_stats.targeted_count, 0)"
        )) + """
                ORDER BY response_rate ASC,
                    (COALESCE(recipient_stats.targeted_count, 0) - COALESCE(recipient_stats.submitted_count, 0)) DESC,
                    s.survey_id DESC
                """);
        applyParameters(query, filter);
        return rows(query).stream()
                .map(row -> new AdminAnalyticsAttentionSurveyResponse(
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

    private String buildWhereClause(AnalyticsFilter filter) {
        return buildWhereClause(filter, List.of());
    }

    private String buildWhereClause(AnalyticsFilter filter, List<String> additionalClauses) {
        List<String> clauses = new ArrayList<>();
        if (filter.startDateFrom() != null) {
            clauses.add("s.start_date IS NOT NULL AND s.start_date >= :startDateFrom");
        }
        if (filter.endDateTo() != null) {
            clauses.add("s.end_date IS NOT NULL AND s.end_date < :endDateTo");
        }
        if (filter.departmentId() != null) {
            clauses.add("first_assignment.subject_type = 'DEPARTMENT' AND first_assignment.subject_value = :departmentId");
        }
        clauses.addAll(additionalClauses);
        return clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
    }

    private void applyParameters(Query query, AnalyticsFilter filter) {
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

    private record AnalyticsFilter(LocalDate startDateFrom, LocalDate endDateTo, Integer departmentId) {
    }
}
