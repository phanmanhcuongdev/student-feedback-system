package com.ttcs.backend.adapter.out.persistence.surveyaisummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyAiSummaryRepository extends JpaRepository<SurveyAiSummaryEntity, Integer> {
    Optional<SurveyAiSummaryEntity> findFirstBySurvey_IdOrderByCreatedAtDesc(Integer surveyId);
}
