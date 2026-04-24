package com.ttcs.backend.adapter.out.persistence.survey;

import com.ttcs.backend.adapter.out.persistence.admin.AdminRepository;
import com.ttcs.backend.application.domain.model.SurveyStatus;
import com.ttcs.backend.application.port.out.LoadStudentSurveysQuery;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.StudentSurveySearchItem;
import com.ttcs.backend.application.port.out.StudentSurveySearchPage;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyPersistenceAdapter implements LoadSurveyPort, com.ttcs.backend.application.port.out.SaveSurveyPort {
    private final SurveyRepository surveyRepository;
    private final AdminRepository adminRepository;
    private final EntityManager entityManager;

    @Override
    public Optional<Survey> loadById(Integer surveyId) {
        return surveyRepository.findById(surveyId)
                .map(SurveyMapper::toDomain);
    }

    @Override
    public List<Survey> loadAll(){
        return surveyRepository.findAll()
                .stream()
                .map(SurveyMapper::toDomain)
                .toList();
    }

    @Override
    public StudentSurveySearchPage loadStudentSurveyPage(LoadStudentSurveysQuery query) {
        int page = Math.max(query.page(), 0);
        int size = Math.min(Math.max(query.size(), 1), 100);

        String runtimeSql = """
                CASE
                    WHEN s.start_date IS NOT NULL AND s.start_date > GETDATE() THEN 'NOT_OPEN'
                    WHEN s.end_date IS NOT NULL AND s.end_date < GETDATE() THEN 'CLOSED'
                    ELSE 'OPEN'
                END
                """;

        String whereClause = buildStudentSurveyWhereClause(query, runtimeSql);
        String orderClause = buildStudentSurveyOrderClause(query.sortBy(), query.sortDir());

        Query itemsQuery = entityManager.createNativeQuery("""
                SELECT
                    s.survey_id,
                    s.title,
                    s.description,
                    s.start_date,
                    s.end_date,
                    a.user_id,
                    """ + runtimeSql + """
                    AS runtime_status,
                    CASE
                        WHEN sr.submitted_at IS NOT NULL THEN 1
                        ELSE 0
                    END AS submitted
                FROM Survey_Recipient sr
                INNER JOIN Survey s ON s.survey_id = sr.survey_id
                INNER JOIN Admin a ON a.user_id = s.created_by
                """ + whereClause + orderClause + " OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
        applyStudentSurveyQueryParameters(itemsQuery, query);
        itemsQuery.setParameter("offset", page * size);
        itemsQuery.setParameter("size", size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = itemsQuery.getResultList();
        List<StudentSurveySearchItem> items = rows.stream().map(this::toStudentSurveySearchItem).toList();

        Query countQuery = entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM Survey_Recipient sr
                INNER JOIN Survey s ON s.survey_id = sr.survey_id
                INNER JOIN Admin a ON a.user_id = s.created_by
                """ + whereClause);
        applyStudentSurveyQueryParameters(countQuery, query);
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        return new StudentSurveySearchPage(items, page, size, totalElements, totalPages);
    }

    @Override
    public Survey save(Survey survey) {
        SurveyEntity entity = SurveyMapper.toEntity(survey);
        if (survey.getCreatedBy() == null) {
            throw new IllegalArgumentException("Survey creator is required");
        }
        entity.setCreatedBy(adminRepository.getReferenceById(survey.getCreatedBy()));
        SurveyEntity saved = surveyRepository.save(entity);
        return SurveyMapper.toDomain(saved);
    }

    private void applyStudentSurveyQueryParameters(Query query, LoadStudentSurveysQuery request) {
        query.setParameter("studentId", request.studentId());
        if (request.status() != null && !request.status().isBlank()) {
            query.setParameter("status", request.status().trim().toUpperCase());
        }
        if (request.submitted() != null) {
            query.setParameter("submitted", request.submitted() ? 1 : 0);
        }
    }

    private String buildStudentSurveyWhereClause(LoadStudentSurveysQuery query, String runtimeSql) {
        StringBuilder where = new StringBuilder(
                """
                 WHERE sr.student_id = :studentId
                   AND s.lifecycle_state = 'PUBLISHED'
                   AND s.hidden = 0
                """
        );
        where.append(" AND ").append(runtimeSql).append(" <> 'CLOSED'");
        if (query.status() != null && !query.status().isBlank() && !"ALL".equalsIgnoreCase(query.status())) {
            where.append(" AND ").append(runtimeSql).append(" = :status");
        }
        if (query.submitted() != null) {
            where.append(" AND CASE WHEN sr.submitted_at IS NOT NULL THEN 1 ELSE 0 END = :submitted");
        }
        return where.toString();
    }

    private String buildStudentSurveyOrderClause(String sortBy, String sortDir) {
        String normalizedSortBy = sortBy == null ? "endDate" : sortBy.trim();
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";

        String expression = switch (normalizedSortBy) {
            case "startDate" -> "s.start_date";
            case "assignedAt" -> "sr.assigned_at";
            case "title" -> "s.title";
            default -> "s.end_date";
        };

        return " ORDER BY " + expression + " " + direction + ", s.survey_id DESC";
    }

    private StudentSurveySearchItem toStudentSurveySearchItem(Object[] row) {
        return new StudentSurveySearchItem(
                ((Number) row[0]).intValue(),
                (String) row[1],
                (String) row[2],
                toLocalDateTime(row[3]),
                toLocalDateTime(row[4]),
                ((Number) row[5]).intValue(),
                SurveyStatus.valueOf(row[6].toString()),
                ((Number) row[7]).intValue() == 1
        );
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
