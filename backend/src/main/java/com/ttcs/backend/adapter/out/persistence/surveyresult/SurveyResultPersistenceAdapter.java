package com.ttcs.backend.adapter.out.persistence.surveyresult;

import com.ttcs.backend.adapter.out.persistence.DepartmentRepository;
import com.ttcs.backend.adapter.out.persistence.question.QuestionEntity;
import com.ttcs.backend.adapter.out.persistence.reporting.ReportingSqlFragments;
import com.ttcs.backend.adapter.out.persistence.responsedetail.ResponseDetailEntity;
import com.ttcs.backend.adapter.out.persistence.responsedetail.ResponseDetailRepository;
import com.ttcs.backend.adapter.out.persistence.surveyassignment.SurveyAssignmentEntity;
import com.ttcs.backend.adapter.out.persistence.surveyassignment.SurveyAssignmentRepository;
import com.ttcs.backend.adapter.out.persistence.surveyrecipient.SurveyRecipientRepository;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyMapper;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyRepository;
import com.ttcs.backend.adapter.out.persistence.surveyresponse.SurveyResponseRepository;
import com.ttcs.backend.application.port.in.resultview.QuestionStatisticsResult;
import com.ttcs.backend.application.port.in.resultview.RatingBreakdownResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.out.LoadSurveyReportPort;
import com.ttcs.backend.application.port.out.LoadSurveyResultsQuery;
import com.ttcs.backend.application.port.out.LoadSurveyResultPort;
import com.ttcs.backend.application.port.out.SurveyReport;
import com.ttcs.backend.application.port.out.SurveyReportQuestion;
import com.ttcs.backend.application.port.out.SurveyReportRatingBreakdown;
import com.ttcs.backend.application.port.out.SurveyResultMetrics;
import com.ttcs.backend.application.port.out.SurveyResultSearchItem;
import com.ttcs.backend.application.port.out.SurveyResultSearchPage;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyResultPersistenceAdapter implements LoadSurveyResultPort, LoadSurveyReportPort {

    private final EntityManager entityManager;
    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final ResponseDetailRepository responseDetailRepository;
    private final SurveyRecipientRepository surveyRecipientRepository;
    private final SurveyAssignmentRepository surveyAssignmentRepository;
    private final DepartmentRepository departmentRepository;

    private static final String RUNTIME_STATUS_SQL = ReportingSqlFragments.runtimeStatus("s");

    private static final String RESPONSE_RATE_SQL = ReportingSqlFragments.recipientResponseRate("recipient_stats");

    private static final String BASE_FROM = """
            FROM Survey s
            OUTER APPLY (
                SELECT TOP 1
                    sa.evaluator_type,
                    sa.subject_type,
                    sa.subject_value
                FROM Survey_Assignment sa
                WHERE sa.survey_id = s.survey_id
                ORDER BY sa.id ASC
            ) first_assignment
            LEFT JOIN Department d
                ON d.dept_id = first_assignment.subject_value
                AND first_assignment.subject_type = 'DEPARTMENT'
            """ + ReportingSqlFragments.recipientStatsJoin("s", "recipient_stats") + """
            LEFT JOIN (
                SELECT
                    rsp.survey_id,
                    COUNT(*) AS response_count
                FROM Survey_Response rsp
                GROUP BY rsp.survey_id
            ) response_stats ON response_stats.survey_id = s.survey_id
            """;

    @Override
    public SurveyResultSearchPage loadPage(LoadSurveyResultsQuery query) {
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
                    """ + RUNTIME_STATUS_SQL + """
                    AS status,
                    s.lifecycle_state,
                    """ + RUNTIME_STATUS_SQL + """
                    AS runtime_status,
                    CASE
                        WHEN first_assignment.subject_type = 'DEPARTMENT' THEN 'DEPARTMENT'
                        ELSE 'ALL_STUDENTS'
                    END AS recipient_scope,
                    d.name AS recipient_department_name,
                    COALESCE(response_stats.response_count, 0) AS response_count,
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
        List<SurveyResultSearchItem> items = rows.stream()
                .map(this::toSearchItem)
                .toList();

        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + BASE_FROM + whereClause);
        applyQueryParameters(countQuery, query);
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        Query metricsQuery = entityManager.createNativeQuery(
                "SELECT "
                        + "COUNT(*) AS total_results, "
                        + "SUM(CASE WHEN " + RUNTIME_STATUS_SQL + " = 'OPEN' THEN 1 ELSE 0 END) AS total_open, "
                        + "SUM(CASE WHEN " + RUNTIME_STATUS_SQL + " = 'CLOSED' THEN 1 ELSE 0 END) AS total_closed, "
                        + "AVG(CAST(" + RESPONSE_RATE_SQL + " AS FLOAT)) AS average_response_rate, "
                        + "SUM(COALESCE(recipient_stats.submitted_count, 0)) AS total_submitted, "
                        + "SUM(COALESCE(response_stats.response_count, 0)) AS total_responses "
                        + BASE_FROM + whereClause
        );
        applyQueryParameters(metricsQuery, query);
        Object[] metricsRow = (Object[]) metricsQuery.getSingleResult();

        return new SurveyResultSearchPage(
                items,
                page,
                size,
                totalElements,
                totalPages,
                new SurveyResultMetrics(
                        getLong(metricsRow[0]),
                        getLong(metricsRow[1]),
                        getLong(metricsRow[2]),
                        getDouble(metricsRow[3]),
                        getLong(metricsRow[4]),
                        getLong(metricsRow[5])
                )
        );
    }

    @Override
    public Optional<SurveyResultDetailResult> loadSurveyResult(Integer surveyId) {
        SurveyEntity survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null) {
            return Optional.empty();
        }

        List<QuestionEntity> questions = responseDetailRepository.findQuestionsBySurveyId(surveyId);
        List<ResponseDetailEntity> details = responseDetailRepository.findAllBySurveyIdForResults(surveyId);
        long responseCount = surveyResponseRepository.countBySurvey_Id(surveyId);
        RecipientSummary recipientSummary = recipientSummary(surveyId);

        Map<Integer, List<ResponseDetailEntity>> detailsByQuestionId = new LinkedHashMap<>();
        for (QuestionEntity question : questions) {
            detailsByQuestionId.put(question.getId(), new ArrayList<>());
        }
        for (ResponseDetailEntity detail : details) {
            QuestionEntity question = detail.getQuestion();
            if (question != null) {
                detailsByQuestionId.computeIfAbsent(question.getId(), key -> new ArrayList<>()).add(detail);
            }
        }

        List<QuestionStatisticsResult> questionResults = questions.stream()
                .map(question -> toQuestionStatistics(question, detailsByQuestionId.getOrDefault(question.getId(), List.of())))
                .toList();

        return Optional.of(new SurveyResultDetailResult(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                SurveyMapper.toDomain(survey).status().name(),
                survey.getLifecycleState(),
                SurveyMapper.toDomain(survey).status().name(),
                recipientScope(surveyId),
                recipientDepartmentName(surveyId),
                responseCount,
                recipientSummary.targetedCount(),
                recipientSummary.openedCount(),
                recipientSummary.submittedCount(),
                recipientSummary.responseRate(),
                questionResults
        ));
    }

    @Override
    public Optional<SurveyReport> loadSurveyReport(Integer surveyId) {
        SurveyEntity survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null) {
            return Optional.empty();
        }

        List<QuestionEntity> questions = responseDetailRepository.findQuestionsBySurveyId(surveyId);
        List<ResponseDetailEntity> details = responseDetailRepository.findAllBySurveyIdForResults(surveyId);
        RecipientSummary recipientSummary = recipientSummary(surveyId);

        Map<Integer, List<ResponseDetailEntity>> detailsByQuestionId = new LinkedHashMap<>();
        for (QuestionEntity question : questions) {
            detailsByQuestionId.put(question.getId(), new ArrayList<>());
        }
        for (ResponseDetailEntity detail : details) {
            QuestionEntity question = detail.getQuestion();
            if (question != null) {
                detailsByQuestionId.computeIfAbsent(question.getId(), key -> new ArrayList<>()).add(detail);
            }
        }

        return Optional.of(new SurveyReport(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getLifecycleState(),
                SurveyMapper.toDomain(survey).status().name(),
                recipientScope(surveyId),
                recipientDepartmentName(surveyId),
                recipientSummary.targetedCount(),
                recipientSummary.openedCount(),
                recipientSummary.submittedCount(),
                recipientSummary.responseRate(),
                questions.stream()
                        .map(question -> toSurveyReportQuestion(question, detailsByQuestionId.getOrDefault(question.getId(), List.of())))
                        .toList()
        ));
    }

    private RecipientSummary recipientSummary(Integer surveyId) {
        List<com.ttcs.backend.adapter.out.persistence.surveyrecipient.SurveyRecipientEntity> recipients =
                surveyRecipientRepository.findBySurvey_IdOrderByIdAsc(surveyId);
        long targetedCount = recipients.size();
        long openedCount = recipients.stream().filter(item -> item.getOpenedAt() != null).count();
        long submittedCount = recipients.stream().filter(item -> item.getSubmittedAt() != null).count();
        double responseRate = targetedCount == 0 ? 0.0 : (submittedCount * 100.0) / targetedCount;
        return new RecipientSummary(targetedCount, openedCount, submittedCount, responseRate);
    }

    private QuestionStatisticsResult toQuestionStatistics(QuestionEntity question, List<ResponseDetailEntity> details) {
        String type = question.getType();
        long responseCount = details.size();

        if ("RATING".equalsIgnoreCase(type)) {
            Map<Integer, Long> counts = new LinkedHashMap<>();
            for (int rating = 1; rating <= 5; rating++) {
                counts.put(rating, 0L);
            }

            double total = 0;
            long ratedCount = 0;
            for (ResponseDetailEntity detail : details) {
                Integer rating = detail.getRating();
                if (rating != null) {
                    counts.put(rating, counts.getOrDefault(rating, 0L) + 1);
                    total += rating;
                    ratedCount++;
                }
            }

            Double average = ratedCount == 0 ? null : total / ratedCount;

            return new QuestionStatisticsResult(
                    question.getId(),
                    question.getContent(),
                    type,
                    responseCount,
                    average,
                    counts.entrySet().stream()
                            .map(entry -> new RatingBreakdownResult(entry.getKey(), entry.getValue()))
                            .toList(),
                    List.of()
            );
        }

        List<String> comments = details.stream()
                .map(ResponseDetailEntity::getComment)
                .filter(comment -> comment != null && !comment.isBlank())
                .toList();

        return new QuestionStatisticsResult(
                question.getId(),
                question.getContent(),
                type,
                responseCount,
                null,
                List.of(),
                comments
        );
    }

    private SurveyReportQuestion toSurveyReportQuestion(QuestionEntity question, List<ResponseDetailEntity> details) {
        String type = question.getType();
        long responseCount = details.size();

        if ("RATING".equalsIgnoreCase(type)) {
            Map<Integer, Long> counts = new LinkedHashMap<>();
            for (int rating = 1; rating <= 5; rating++) {
                counts.put(rating, 0L);
            }

            double total = 0;
            long ratedCount = 0;
            for (ResponseDetailEntity detail : details) {
                Integer rating = detail.getRating();
                if (rating != null) {
                    counts.put(rating, counts.getOrDefault(rating, 0L) + 1);
                    total += rating;
                    ratedCount++;
                }
            }

            Double average = ratedCount == 0 ? null : total / ratedCount;

            return new SurveyReportQuestion(
                    question.getId(),
                    question.getContent(),
                    type,
                    responseCount,
                    average,
                    counts.entrySet().stream()
                            .map(entry -> new SurveyReportRatingBreakdown(entry.getKey(), entry.getValue()))
                            .toList(),
                    List.of()
            );
        }

        List<String> comments = details.stream()
                .map(ResponseDetailEntity::getComment)
                .filter(comment -> comment != null && !comment.isBlank())
                .toList();

        return new SurveyReportQuestion(
                question.getId(),
                question.getContent(),
                type,
                responseCount,
                null,
                List.of(),
                comments
        );
    }

    private String recipientScope(Integer surveyId) {
        SurveyAssignmentEntity assignment = firstAssignment(surveyId);
        if (assignment == null) {
            return "ALL_STUDENTS";
        }
        return "DEPARTMENT".equalsIgnoreCase(assignment.getSubjectType()) ? "DEPARTMENT" : "ALL_STUDENTS";
    }

    private String recipientDepartmentName(Integer surveyId) {
        SurveyAssignmentEntity assignment = firstAssignment(surveyId);
        if (assignment == null) {
            return null;
        }
        if (!"DEPARTMENT".equalsIgnoreCase(assignment.getSubjectType())) {
            return null;
        }
        if (assignment.getSubjectValue() == null) {
            return null;
        }
        return departmentRepository.findById(assignment.getSubjectValue())
                .map(item -> item.getName())
                .orElse(null);
    }

    private SurveyAssignmentEntity firstAssignment(Integer surveyId) {
        return surveyAssignmentRepository.findBySurveyIdOrderByIdAsc(surveyId).stream().findFirst().orElse(null);
    }

    private SurveyResultSearchItem toSearchItem(Object[] row) {
        return new SurveyResultSearchItem(
                ((Number) row[0]).intValue(),
                (String) row[1],
                (String) row[2],
                toLocalDateTime(row[3]),
                toLocalDateTime(row[4]),
                row[5] != null ? row[5].toString() : null,
                row[6] != null ? row[6].toString() : null,
                row[7] != null ? row[7].toString() : null,
                row[8] != null ? row[8].toString() : "ALL_STUDENTS",
                (String) row[9],
                getLong(row[10]),
                getLong(row[11]),
                getLong(row[12]),
                getLong(row[13]),
                getDouble(row[14])
        );
    }

    private void applyQueryParameters(Query nativeQuery, LoadSurveyResultsQuery query) {
        if (query.keyword() != null && !query.keyword().isBlank()) {
            nativeQuery.setParameter("keyword", "%" + query.keyword().trim().toLowerCase() + "%");
        }
        if (query.lifecycleState() != null && !query.lifecycleState().isBlank()) {
            nativeQuery.setParameter("lifecycleState", query.lifecycleState().trim().toUpperCase());
        }
        if (query.runtimeStatus() != null && !query.runtimeStatus().isBlank()) {
            nativeQuery.setParameter("runtimeStatus", query.runtimeStatus().trim().toUpperCase());
        }
        if (query.recipientScope() != null && !query.recipientScope().isBlank()) {
            nativeQuery.setParameter("recipientScope", query.recipientScope().trim().toUpperCase());
        }
        if (query.startDateFrom() != null) {
            nativeQuery.setParameter("startDateFrom", query.startDateFrom().atStartOfDay());
        }
        if (query.endDateTo() != null) {
            nativeQuery.setParameter("endDateTo", query.endDateTo().plusDays(1).atStartOfDay());
        }
        if (query.lecturerDepartmentId() != null) {
            nativeQuery.setParameter("lecturerDepartmentId", query.lecturerDepartmentId());
        }
    }

    private String buildWhereClause(LoadSurveyResultsQuery query) {
        List<String> clauses = new ArrayList<>();

        if (query.keyword() != null && !query.keyword().isBlank()) {
            clauses.add("LOWER(COALESCE(s.title, '')) LIKE :keyword");
        }
        if (query.lifecycleState() != null && !query.lifecycleState().isBlank()) {
            clauses.add("s.lifecycle_state = :lifecycleState");
        }
        if (query.runtimeStatus() != null && !query.runtimeStatus().isBlank()) {
            clauses.add(RUNTIME_STATUS_SQL + " = :runtimeStatus");
        }
        if (query.recipientScope() != null && !query.recipientScope().isBlank()) {
            if ("DEPARTMENT".equalsIgnoreCase(query.recipientScope())) {
                clauses.add("first_assignment.subject_type = 'DEPARTMENT'");
            } else if ("ALL_STUDENTS".equalsIgnoreCase(query.recipientScope())) {
                clauses.add("(first_assignment.subject_type = 'ALL' OR first_assignment.subject_type IS NULL)");
            }
        }
        if (query.startDateFrom() != null) {
            clauses.add("s.start_date IS NOT NULL AND s.start_date >= :startDateFrom");
        }
        if (query.endDateTo() != null) {
            clauses.add("s.end_date IS NOT NULL AND s.end_date < :endDateTo");
        }
        if (query.lecturerDepartmentId() != null) {
            clauses.add("""
                    EXISTS (
                        SELECT 1
                        FROM Survey_Assignment lecturer_scope
                        WHERE lecturer_scope.survey_id = s.survey_id
                            AND lecturer_scope.evaluator_type = 'STUDENT'
                            AND lecturer_scope.subject_type = 'DEPARTMENT'
                            AND lecturer_scope.subject_value = :lecturerDepartmentId
                    )
                    """);
        }

        if (clauses.isEmpty()) {
            return "";
        }

        return " WHERE " + String.join(" AND ", clauses);
    }

    private String buildOrderClause(String sortBy, String sortDir) {
        String normalizedSortBy = sortBy == null ? "responseRate" : sortBy.trim();
        String direction = "asc".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";

        String expression = switch (normalizedSortBy) {
            case "title" -> "s.title";
            case "startDate" -> "s.start_date";
            case "endDate" -> "s.end_date";
            case "responseCount" -> "COALESCE(response_stats.response_count, 0)";
            case "targetedCount" -> "COALESCE(recipient_stats.targeted_count, 0)";
            case "submittedCount" -> "COALESCE(recipient_stats.submitted_count, 0)";
            case "lifecycleState" -> "s.lifecycle_state";
            case "runtimeStatus" -> RUNTIME_STATUS_SQL;
            default -> RESPONSE_RATE_SQL;
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

    private record RecipientSummary(long targetedCount, long openedCount, long submittedCount, double responseRate) {
    }
}
