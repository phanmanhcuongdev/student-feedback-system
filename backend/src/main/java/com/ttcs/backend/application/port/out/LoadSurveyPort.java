package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Survey;

import java.util.Optional;

public interface LoadSurveyPort {
    Optional<Survey> loadById(Integer surveyId);
}
