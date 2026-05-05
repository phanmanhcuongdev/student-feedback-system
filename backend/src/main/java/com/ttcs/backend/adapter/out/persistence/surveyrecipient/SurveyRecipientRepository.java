package com.ttcs.backend.adapter.out.persistence.surveyrecipient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SurveyRecipientRepository extends JpaRepository<SurveyRecipientEntity, Integer> {
    Optional<SurveyRecipientEntity> findBySurvey_IdAndStudent_Id(Integer surveyId, Integer studentId);

    List<SurveyRecipientEntity> findBySurvey_IdOrderByIdAsc(Integer surveyId);

    List<SurveyRecipientEntity> findByStudent_IdOrderByAssignedAtDesc(Integer studentId);

    @Query("""
            SELECT recipient
            FROM SurveyRecipientEntity recipient
            JOIN FETCH recipient.survey survey
            JOIN FETCH recipient.student student
            WHERE recipient.submittedAt IS NULL
                AND survey.lifecycleState = 'PUBLISHED'
                AND survey.hidden = false
                AND survey.endDate IS NOT NULL
                AND survey.endDate >= :now
                AND survey.endDate <= :deadlineTo
            ORDER BY survey.endDate ASC, recipient.id ASC
            """)
    List<SurveyRecipientEntity> findUnsubmittedClosingBetween(
            @Param("now") LocalDateTime now,
            @Param("deadlineTo") LocalDateTime deadlineTo
    );
}
