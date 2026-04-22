package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Question;

import java.util.List;

public interface SaveQuestionPort {
    void saveAll(List<Question> questions);

    void replaceSurveyQuestions(Integer surveyId, List<Question> questions);

    default List<Question> saveAllReturning(List<Question> questions) {
        saveAll(questions);
        return questions;
    }

    default List<Question> replaceSurveyQuestionsReturning(Integer surveyId, List<Question> questions) {
        replaceSurveyQuestions(surveyId, questions);
        return questions;
    }
}
