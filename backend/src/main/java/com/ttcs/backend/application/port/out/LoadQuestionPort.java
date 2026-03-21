package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Question;

import java.util.List;

public interface LoadQuestionPort {
    List<Question> loadBySurveyId(Integer surveyId);
}
