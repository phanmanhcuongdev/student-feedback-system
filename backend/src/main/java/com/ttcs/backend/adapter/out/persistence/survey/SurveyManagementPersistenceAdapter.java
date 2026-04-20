package com.ttcs.backend.adapter.out.persistence.survey;

import com.ttcs.backend.adapter.out.persistence.DepartmentRepository;
import com.ttcs.backend.adapter.out.persistence.department.DepartmentMapper;
import com.ttcs.backend.adapter.out.persistence.reporting.ReportingSqlFragments;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.port.out.admin.ManageSurveyPort;
import com.ttcs.backend.application.port.out.admin.ManagedSurveyMetrics;
import com.ttcs.backend.application.port.out.admin.ManagedSurveySearchItem;
import com.ttcs.backend.application.port.out.admin.ManagedSurveySearchPage;
import com.ttcs.backend.application.port.out.admin.ManageSurveysQuery;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyManagementPersistenceAdapter implements ManageSurveyPort {

    private final EntityManager entityManager;
    private final DepartmentRepository departmentRepository;

    private static final String RUNTIME_STATUS_SQL = ReportingSqlFragments.runtimeStatus("s");

    private static final String RESPONSE_RATE_SQL = ReportingSqlFragments.recipientResponseRate("recipient_stats");

    private static final String BASE_FROM = """
            FROM Survey s
            LEFT JOIN Survey_Assignment sa ON sa.survey_id = s.survey_id
            LEFT JOIN Department d ON d.dept_id = sa.subject_value AND sa.subject_type = 'DEPARTMENT'
            """ + ReportingSqlFragments.recipientStatsJoin("s", "recipient_stats");

    @Override
    public ManagedSurveySearchPage loadPage(ManageSurveysQuery query) {
        int page = Math.max(query.page(), 0);
        int size = Math.min(Math.max(query.size(), 1), 100);

        String whereClause = buildWhereClause(query);
        String orderClause = buildOrderClause(query.sortBy(), query.sortDir());

        Query itemsQuery = entityManager.createNativeQuery("""
                SELECT
                    s.survey_id,
                    s.title,
                    s.description,
                    s.start_date,
                    s.end_date,
                    s.lifecycle_state,
                    """ + RUNTIME_STATUS_SQL + """
                    AS runtime_status,
                    s.hidden,
                    CASE WHEN sa.subject_type = 'DEPARTMENT' THEN 'DEPARTMENT' ELSE 'ALL_STUDENTS' END AS recipient_scope,
                    sa.subject_value AS recipient_department_id,
                    d.name AS recipient_department_name,
                    COALESCE(recipient_stats.submitted_count, 0) AS response_count,
                    COALESCE(recipient_stats.targeted_count, 0) AS targeted_count,
                    COALESCE(recipient_stats.opened_count, 0) AS opened_count,
                    COALESCE(recipient_stats.submitted_count, 0) AS submitted_count,
                    """ + RESPONSE_RATE_SQL + """
                    AS response_rate
                """ + BASE_FROM + whereClause + orderClause + " OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
        applyQueryParameters(itemsQuery, query);
        itemsQuery.setParameter("offset", page * size);
        itemsQuery.setParameter("size", size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = itemsQuery.getResultList();
        List<ManagedSurveySearchItem> items = rows.stream().map(this::toSearchItem).toList();

        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + BASE_FROM + whereClause);
        applyQueryParameters(countQuery, query);
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        Query metricsQuery = entityManager.createNativeQuery(
                "SELECT "
                        + "COUNT(*) AS total_surveys, "
                        + "SUM(CASE WHEN s.lifecycle_state = 'DRAFT' THEN 1 ELSE 0 END) AS total_drafts, "
                        + "SUM(CASE WHEN s.lifecycle_state = 'PUBLISHED' THEN 1 ELSE 0 END) AS total_published, "
                        + "SUM(CASE WHEN " + RUNTIME_STATUS_SQL + " = 'OPEN' THEN 1 ELSE 0 END) AS total_open, "
                        + "SUM(CASE WHEN s.lifecycle_state IN ('CLOSED', 'ARCHIVED') OR " + RUNTIME_STATUS_SQL + " = 'CLOSED' THEN 1 ELSE 0 END) AS total_closed, "
                        + "SUM(CASE WHEN s.hidden = 1 THEN 1 ELSE 0 END) AS total_hidden "
                        + "FROM Survey s"
        );
        Object[] metricsRow = (Object[]) metricsQuery.getSingleResult();

        return new ManagedSurveySearchPage(
                items,
                page,
                size,
                totalElements,
                totalPages,
                new ManagedSurveyMetrics(
                        getLong(metricsRow[0]),
                        getLong(metricsRow[1]),
                        getLong(metricsRow[2]),
                        getLong(metricsRow[3]),
                        getLong(metricsRow[4]),
                        getLong(metricsRow[5])
                )
        );
    }

    @Override
    public List<Department> loadDepartments() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(DepartmentMapper::toDomain)
                .toList();
    }

    private ManagedSurveySearchItem toSearchItem(Object[] row) {
        return new ManagedSurveySearchItem(
                ((Number) row[0]).intValue(),
                (String) row[1],
                (String) row[2],
                toLocalDateTime(row[3]),
                toLocalDateTime(row[4]),
                row[5] != null ? row[5].toString() : null,
                row[6] != null ? row[6].toString() : null,
                row[7] instanceof Boolean bool ? bool : ((Number) row[7]).intValue() == 1,
                row[8] != null ? row[8].toString() : "ALL_STUDENTS",
                row[9] == null ? null : ((Number) row[9]).intValue(),
                (String) row[10],
                getLong(row[11]),
                getLong(row[12]),
                getLong(row[13]),
                getLong(row[14]),
                getDouble(row[15])
        );
    }

    private void applyQueryParameters(Query query, ManageSurveysQuery request) {
        if (request.keyword() != null && !request.keyword().isBlank()) {
            query.setParameter("keyword", "%" + request.keyword().trim().toLowerCase() + "%");
        }
        if (request.lifecycleState() != null && !request.lifecycleState().isBlank()) {
            query.setParameter("lifecycleState", request.lifecycleState().trim().toUpperCase());
        }
        if (request.runtimeStatus() != null && !request.runtimeStatus().isBlank()) {
            query.setParameter("runtimeStatus", request.runtimeStatus().trim().toUpperCase());
        }
        if (request.hidden() != null) {
            query.setParameter("hidden", request.hidden());
        }
        if (request.recipientScope() != null && !request.recipientScope().isBlank()) {
            query.setParameter("recipientScope", request.recipientScope().trim().toUpperCase());
        }
        if (request.startDateFrom() != null) {
            query.setParameter("startDateFrom", request.startDateFrom().atStartOfDay());
        }
        if (request.endDateTo() != null) {
            query.setParameter("endDateTo", request.endDateTo().plusDays(1).atStartOfDay());
        }
    }

    private String buildWhereClause(ManageSurveysQuery query) {
        List<String> clauses = new ArrayList<>();

        if (query.keyword() != null && !query.keyword().isBlank()) {
            clauses.add("""
                    (
                        LOWER(COALESCE(s.title, '')) LIKE :keyword
                        OR LOWER(COALESCE(s.description, '')) LIKE :keyword
                    )
                    """);
        }
        if (query.lifecycleState() != null && !query.lifecycleState().isBlank()) {
            clauses.add("s.lifecycle_state = :lifecycleState");
        }
        if (query.runtimeStatus() != null && !query.runtimeStatus().isBlank()) {
            clauses.add(RUNTIME_STATUS_SQL + " = :runtimeStatus");
        }
        if (query.hidden() != null) {
            clauses.add("s.hidden = :hidden");
        }
        if (query.recipientScope() != null && !query.recipientScope().isBlank()) {
            if ("DEPARTMENT".equalsIgnoreCase(query.recipientScope())) {
                clauses.add("sa.subject_type = 'DEPARTMENT'");
            } else if ("ALL_STUDENTS".equalsIgnoreCase(query.recipientScope())) {
                clauses.add("(sa.subject_type = 'ALL' OR sa.subject_type IS NULL)");
            }
        }
        if (query.startDateFrom() != null) {
            clauses.add("s.start_date IS NOT NULL AND s.start_date >= :startDateFrom");
        }
        if (query.endDateTo() != null) {
            clauses.add("s.end_date IS NOT NULL AND s.end_date < :endDateTo");
        }

        if (clauses.isEmpty()) {
            return "";
        }

        return " WHERE " + String.join(" AND ", clauses);
    }

    private String buildOrderClause(String sortBy, String sortDir) {
        String normalizedSortBy = sortBy == null ? "startDate" : sortBy.trim();
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";

        String expression = switch (normalizedSortBy) {
            case "title" -> "s.title";
            case "endDate" -> "s.end_date";
            case "responseRate" -> RESPONSE_RATE_SQL;
            case "targetedCount" -> "COALESCE(recipient_stats.targeted_count, 0)";
            case "openedCount" -> "COALESCE(recipient_stats.opened_count, 0)";
            case "submittedCount" -> "COALESCE(recipient_stats.submitted_count, 0)";
            case "lifecycle" -> "s.lifecycle_state";
            case "runtime" -> RUNTIME_STATUS_SQL;
            default -> "s.start_date";
        };

        return " ORDER BY " + expression + " " + direction + ", s.survey_id DESC";
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

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        throw new IllegalArgumentException("Unsupported datetime value: " + value.getClass().getName());
    }
}
