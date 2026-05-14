package com.ttcs.backend.adapter.out.persistence.surveyaisummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyAiSummaryThemeEmbeddingRepository extends JpaRepository<SurveyAiSummaryThemeEmbeddingEntity, Integer> {
    List<SurveyAiSummaryThemeEmbeddingEntity> findBySurvey_IdAndSummary_IdOrderByThemeTypeAscThemeIndexAsc(Integer surveyId, Integer summaryId);
}
