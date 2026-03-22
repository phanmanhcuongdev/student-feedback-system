package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.SurveyResponse;

public interface SaveSurveyResponsePort {
    SurveyResponse save(SurveyResponse surveyResponse);
}