package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Survey;

import java.util.List;
import java.util.Optional;

public interface LoadSurveyPort {
    Optional<Survey> loadById(Integer surveyId);
    List<Survey> loadAll();
}
