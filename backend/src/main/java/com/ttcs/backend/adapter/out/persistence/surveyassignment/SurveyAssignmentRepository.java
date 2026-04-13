package com.ttcs.backend.adapter.out.persistence.surveyassignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyAssignmentRepository extends JpaRepository<SurveyAssignmentEntity, Integer> {
    List<SurveyAssignmentEntity> findBySurveyIdOrderByIdAsc(Integer surveyId);

    void deleteBySurveyId(Integer surveyId);
}
