package com.ttcs.backend.adapter.out.persistence.surveyaisummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyAiPendingChangeRepository extends JpaRepository<SurveyAiPendingChangeEntity, Integer> {
    boolean existsByResponseDetail_Id(Integer responseDetailId);

    List<SurveyAiPendingChangeEntity> findBySurvey_IdAndProcessedFalse(Integer surveyId);
}
