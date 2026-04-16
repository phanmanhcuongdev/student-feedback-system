package com.ttcs.backend.adapter.out.persistence.surveyrecipient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyRecipientRepository extends JpaRepository<SurveyRecipientEntity, Integer> {
    Optional<SurveyRecipientEntity> findBySurvey_IdAndStudent_Id(Integer surveyId, Integer studentId);

    List<SurveyRecipientEntity> findBySurvey_IdOrderByIdAsc(Integer surveyId);

    List<SurveyRecipientEntity> findByStudent_IdOrderByAssignedAtDesc(Integer studentId);
}
