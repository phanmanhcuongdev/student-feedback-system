package com.ttcs.backend.adapter.out.persistence.responsedetail;

import com.ttcs.backend.adapter.out.persistence.question.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResponseDetailRepository extends JpaRepository<ResponseDetailEntity, Integer> {
    @Query("""
            select distinct q
            from QuestionEntity q
            where q.survey.id = :surveyId
            order by q.id asc
            """)
    List<QuestionEntity> findQuestionsBySurveyId(Integer surveyId);

    @Query("""
            select rd
            from ResponseDetailEntity rd
            join fetch rd.question q
            join fetch q.survey s
            join fetch rd.response r
            join fetch r.survey rs
            where rs.id = :surveyId
            order by q.id asc, rd.id asc
            """)
    List<ResponseDetailEntity> findAllBySurveyIdForResults(Integer surveyId);
}
