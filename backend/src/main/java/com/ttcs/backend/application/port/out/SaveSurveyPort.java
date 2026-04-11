package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Survey;

public interface SaveSurveyPort {
    Survey save(Survey survey);
}
