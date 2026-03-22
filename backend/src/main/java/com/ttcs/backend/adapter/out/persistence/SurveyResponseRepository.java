package com.ttcs.backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponseEntity, Integer> {
    boolean existsBySurveyIdAndStudentId(Integer surveyId, Integer studentId);
}