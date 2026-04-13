package com.ttcs.backend.adapter.out.persistence.question;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Integer> {
    List<QuestionEntity> findBySurvey_IdOrderByIdAsc(Integer surveyId);
    boolean existsByIdAndSurvey_Id(Integer questionId, Integer surveyId);

    void deleteBySurvey_Id(Integer surveyId);
}
