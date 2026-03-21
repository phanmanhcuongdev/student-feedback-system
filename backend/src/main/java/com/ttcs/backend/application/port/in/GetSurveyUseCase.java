package com.ttcs.backend.application.port.in;

import com.ttcs.backend.adapter.in.web.dto.SurveyResponse;

public interface GetSurveyUseCase {
    SurveyResponse getSurveyById(Integer surveyId);
}
