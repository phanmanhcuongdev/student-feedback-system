package com.ttcs.backend.adapter.out.persistence.surveyaisummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyAiSummaryJobRepository extends JpaRepository<SurveyAiSummaryJobEntity, Integer> {
    Optional<SurveyAiSummaryJobEntity> findFirstBySurvey_IdOrderByCreatedAtDesc(Integer surveyId);
}
