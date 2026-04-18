package com.ttcs.backend.adapter.out.persistence.feedback;

import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentRepository;
import com.ttcs.backend.application.domain.model.Feedback;
import com.ttcs.backend.application.port.out.LoadFeedbackQuery;
import com.ttcs.backend.application.port.out.LoadStudentFeedbackQuery;
import com.ttcs.backend.application.port.out.LoadFeedbackPort;
import com.ttcs.backend.application.port.out.StaffFeedbackSearchItem;
import com.ttcs.backend.application.port.out.StaffFeedbackSearchPage;
import com.ttcs.backend.application.port.out.StudentFeedbackSearchItem;
import com.ttcs.backend.application.port.out.StudentFeedbackSearchPage;
import com.ttcs.backend.application.port.out.SaveFeedbackPort;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class FeedbackPersistenceAdapter implements LoadFeedbackPort, SaveFeedbackPort {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final EntityManager entityManager;

    @Override
    public List<Feedback> loadByStudentId(Integer studentId) {
        return feedbackRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(FeedbackMapper::toDomain)
                .toList();
    }

    @Override
    public StudentFeedbackSearchPage loadStudentPage(LoadStudentFeedbackQuery query) {
        int page = Math.max(query.page(), 0);
        int size = Math.min(Math.max(query.size(), 1), 100);

        String orderClause = buildStudentOrderClause(query.sortBy(), query.sortDir());

        Query itemsQuery = entityManager.createNativeQuery("""
                SELECT
                    f.feedback_id,
                    f.title,
                    f.content,
                    f.created_at
                FROM [Feedback] f
                WHERE f.student_id = :studentId
                """ + orderClause + " OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
        itemsQuery.setParameter("studentId", query.studentId());
        itemsQuery.setParameter("offset", page * size);
        itemsQuery.setParameter("size", size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = itemsQuery.getResultList();
        List<StudentFeedbackSearchItem> items = rows.stream().map(this::toStudentSearchItem).toList();

        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM [Feedback] f WHERE f.student_id = :studentId");
        countQuery.setParameter("studentId", query.studentId());
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        return new StudentFeedbackSearchPage(items, page, size, totalElements, totalPages);
    }

    @Override
    public StaffFeedbackSearchPage loadPage(LoadFeedbackQuery query) {
        int page = Math.max(query.page(), 0);
        int size = Math.min(Math.max(query.size(), 1), 100);

        String whereClause = buildWhereClause(query);
        String orderClause = buildOrderClause(query.sortBy(), query.sortDir());

        Query itemsQuery = entityManager.createNativeQuery("""
                SELECT
                    f.feedback_id,
                    s.user_id,
                    s.name,
                    u.email,
                    f.title,
                    f.content,
                    f.created_at
                FROM [Feedback] f
                INNER JOIN Student s ON s.user_id = f.student_id
                INNER JOIN [User] u ON u.user_id = s.user_id
                LEFT JOIN (
                    SELECT
                        fr.feedback_id,
                        COUNT(*) AS response_count
                    FROM Feedback_Response fr
                    GROUP BY fr.feedback_id
                ) response_stats ON response_stats.feedback_id = f.feedback_id
                """ + whereClause + orderClause + " OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
        applyQueryParameters(itemsQuery, query);
        itemsQuery.setParameter("offset", page * size);
        itemsQuery.setParameter("size", size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = itemsQuery.getResultList();
        List<StaffFeedbackSearchItem> items = rows.stream().map(this::toSearchItem).toList();

        Query countQuery = entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM [Feedback] f
                INNER JOIN Student s ON s.user_id = f.student_id
                INNER JOIN [User] u ON u.user_id = s.user_id
                LEFT JOIN (
                    SELECT
                        fr.feedback_id,
                        COUNT(*) AS response_count
                    FROM Feedback_Response fr
                    GROUP BY fr.feedback_id
                ) response_stats ON response_stats.feedback_id = f.feedback_id
                """ + whereClause);
        applyQueryParameters(countQuery, query);
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        return new StaffFeedbackSearchPage(items, page, size, totalElements, totalPages);
    }

    @Override
    public Optional<Feedback> loadById(Integer feedbackId) {
        return feedbackRepository.findById(feedbackId).map(FeedbackMapper::toDomain);
    }

    @Override
    public Feedback save(Feedback feedback) {
        FeedbackEntity entity = new FeedbackEntity();
        entity.setId(feedback.getId());
        entity.setTitle(feedback.getTitle());
        entity.setContent(feedback.getContent());
        entity.setCreatedAt(feedback.getCreatedAt());

        Integer studentId = feedback.getStudent() != null ? feedback.getStudent().getId() : null;
        if (studentId == null) {
            throw new IllegalArgumentException("Student id is required when saving feedback");
        }

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        entity.setStudent(student);

        return FeedbackMapper.toDomain(feedbackRepository.save(entity));
    }

    private void applyQueryParameters(Query query, LoadFeedbackQuery request) {
        if (request.keyword() != null && !request.keyword().isBlank()) {
            query.setParameter("keyword", "%" + request.keyword().trim().toLowerCase() + "%");
        }
        if (request.status() != null && !request.status().isBlank()) {
            query.setParameter("status", request.status().trim().toUpperCase());
        }
        if (request.createdDate() != null) {
            query.setParameter("createdDateStart", request.createdDate().atStartOfDay());
            query.setParameter("createdDateEnd", request.createdDate().plusDays(1).atStartOfDay());
        }
    }

    private String buildWhereClause(LoadFeedbackQuery query) {
        List<String> clauses = new ArrayList<>();

        if (query.keyword() != null && !query.keyword().isBlank()) {
            clauses.add("""
                    (
                        LOWER(COALESCE(s.name, '')) LIKE :keyword
                        OR LOWER(COALESCE(u.email, '')) LIKE :keyword
                        OR LOWER(COALESCE(f.title, '')) LIKE :keyword
                        OR LOWER(COALESCE(f.content, '')) LIKE :keyword
                    )
                    """);
        }
        if (query.status() != null && !query.status().isBlank()) {
            clauses.add("""
                    (
                        (:status = 'UNRESOLVED' AND COALESCE(response_stats.response_count, 0) = 0)
                        OR (:status = 'RESPONDED' AND COALESCE(response_stats.response_count, 0) > 0)
                    )
                    """);
        }
        if (query.createdDate() != null) {
            clauses.add("f.created_at >= :createdDateStart AND f.created_at < :createdDateEnd");
        }

        if (clauses.isEmpty()) {
            return "";
        }

        return " WHERE " + String.join(" AND ", clauses);
    }

    private String buildOrderClause(String sortBy, String sortDir) {
        String normalizedSortBy = sortBy == null ? "createdAt" : sortBy.trim();
        String direction = "asc".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";

        String expression = switch (normalizedSortBy) {
            case "studentName" -> "s.name";
            case "title" -> "f.title";
            default -> "f.created_at";
        };

        return " ORDER BY " + expression + " " + direction + ", f.feedback_id DESC";
    }

    private String buildStudentOrderClause(String sortBy, String sortDir) {
        String normalizedSortBy = sortBy == null ? "createdAt" : sortBy.trim();
        String direction = "asc".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";

        String expression = switch (normalizedSortBy) {
            case "title" -> "f.title";
            default -> "f.created_at";
        };

        return " ORDER BY " + expression + " " + direction + ", f.feedback_id DESC";
    }

    private StudentFeedbackSearchItem toStudentSearchItem(Object[] row) {
        Object createdAt = row[3];
        LocalDateTime createdDateTime = createdAt instanceof java.sql.Timestamp timestamp
                ? timestamp.toLocalDateTime()
                : (LocalDateTime) createdAt;
        return new StudentFeedbackSearchItem(
                ((Number) row[0]).intValue(),
                (String) row[1],
                (String) row[2],
                createdDateTime
        );
    }

    private StaffFeedbackSearchItem toSearchItem(Object[] row) {
        Object createdAt = row[6];
        LocalDateTime createdDateTime = createdAt instanceof java.sql.Timestamp timestamp
                ? timestamp.toLocalDateTime()
                : (LocalDateTime) createdAt;
        return new StaffFeedbackSearchItem(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).intValue(),
                (String) row[2],
                (String) row[3],
                (String) row[4],
                (String) row[5],
                createdDateTime
        );
    }
}
