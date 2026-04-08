package com.ttcs.backend.application.port.in;

import com.ttcs.backend.application.port.in.result.SurveyDetailResult;

public interface GetSurveyDetailUseCase {
    SurveyDetailResult getSurveyDetail(Integer surveyId);
}
