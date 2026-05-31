package com.ttcs.backend.adapter.out.persistence.surveyrecipient;

import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentRepository;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyRepository;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.SaveSurveyRecipientPort;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyRecipientPersistenceAdapter implements LoadSurveyRecipientPort, SaveSurveyRecipientPort {

    private final SurveyRecipientRepository surveyRecipientRepository;
    private final SurveyRepository surveyRepository;
    private final StudentRepository studentRepository;
    private final EntityManager entityManager;

    @Override
    public Optional<SurveyRecipient> loadBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
        return surveyRecipientRepository.findBySurvey_IdAndStudent_Id(surveyId, studentId)
                .map(SurveyRecipientMapper::toDomain);
    }

    @Override
    public List<SurveyRecipient> loadBySurveyId(Integer surveyId) {
        return surveyRecipientRepository.findBySurvey_IdOrderByIdAsc(surveyId).stream()
                .map(SurveyRecipientMapper::toDomain)
                .toList();
    }

    @Override
    public List<SurveyRecipient> loadByStudentId(Integer studentId) {
        return surveyRecipientRepository.findByStudent_IdOrderByAssignedAtDesc(studentId).stream()
                .map(SurveyRecipientMapper::toDomain)
                .toList();
    }

    @Override
    public SurveyRecipient save(SurveyRecipient recipient) {
        return SurveyRecipientMapper.toDomain(surveyRecipientRepository.save(toEntity(recipient)));
    }

    @Override
    public List<SurveyRecipient> saveAll(List<SurveyRecipient> recipients) {
        return surveyRecipientRepository.saveAll(recipients.stream().map(this::toEntity).toList()).stream()
                .map(SurveyRecipientMapper::toDomain)
                .toList();
    }

    @Override
    public int bulkInsertRecipients(Integer surveyId, Integer departmentId) {
        String sql;
        if (departmentId != null) {
            sql = """
                INSERT INTO Survey_Recipient (survey_id, student_id, assigned_at)
                SELECT :surveyId, s.user_id, :assignedAt
                FROM Student s
                JOIN [User] u ON s.user_id = u.user_id
                WHERE s.status = 'ACTIVE' AND u.verify = 1
                    AND s.dept_id = :departmentId
                    AND s.user_id NOT IN (
                        SELECT sr.student_id FROM Survey_Recipient sr WHERE sr.survey_id = :surveyId
                    )
                """;
        } else {
            sql = """
                INSERT INTO Survey_Recipient (survey_id, student_id, assigned_at)
                SELECT :surveyId, s.user_id, :assignedAt
                FROM Student s
                JOIN [User] u ON s.user_id = u.user_id
                WHERE s.status = 'ACTIVE' AND u.verify = 1
                    AND s.user_id NOT IN (
                        SELECT sr.student_id FROM Survey_Recipient sr WHERE sr.survey_id = :surveyId
                    )
                """;
        }
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("surveyId", surveyId);
        query.setParameter("assignedAt", java.time.LocalDateTime.now());
        if (departmentId != null) {
            query.setParameter("departmentId", departmentId);
        }
        return query.executeUpdate();
    }

    @Override
    @Transactional
    public int bulkInsertCustomRecipients(Integer surveyId, List<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return 0;
        }
        String sql = """
            INSERT INTO Survey_Recipient (survey_id, student_id, assigned_at)
            SELECT :surveyId, s.user_id, :assignedAt
            FROM Student s
            JOIN [User] u ON s.user_id = u.user_id
            WHERE s.status = 'ACTIVE' AND u.verify = 1
                AND s.user_id IN (:studentIds)
                AND s.user_id NOT IN (
                    SELECT sr.student_id FROM Survey_Recipient sr WHERE sr.survey_id = :surveyId
                )
            """;
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("surveyId", surveyId);
        query.setParameter("assignedAt", java.time.LocalDateTime.now());
        query.setParameter("studentIds", studentIds);
        return query.executeUpdate();
    }

    @Override
    @Transactional
    public void syncCustomRecipients(Integer surveyId, List<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            var deleteQuery = entityManager.createNativeQuery("DELETE FROM Survey_Recipient WHERE survey_id = :surveyId");
            deleteQuery.setParameter("surveyId", surveyId);
            deleteQuery.executeUpdate();
            return;
        }

        var deleteOldQuery = entityManager.createNativeQuery(
            "DELETE FROM Survey_Recipient WHERE survey_id = :surveyId AND student_id NOT IN (:studentIds)"
        );
        deleteOldQuery.setParameter("surveyId", surveyId);
        deleteOldQuery.setParameter("studentIds", studentIds);
        deleteOldQuery.executeUpdate();

        bulkInsertCustomRecipients(surveyId, studentIds);
    }

    private SurveyRecipientEntity toEntity(SurveyRecipient recipient) {
        SurveyEntity survey = surveyRepository.findById(recipient.getSurveyId())
                .orElseThrow(() -> new IllegalArgumentException("Survey not found: " + recipient.getSurveyId()));
        StudentEntity student = studentRepository.findById(recipient.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + recipient.getStudentId()));
        return new SurveyRecipientEntity(
                recipient.getId(),
                survey,
                student,
                recipient.getAssignedAt(),
                recipient.getOpenedAt(),
                recipient.getSubmittedAt()
        );
    }
}
