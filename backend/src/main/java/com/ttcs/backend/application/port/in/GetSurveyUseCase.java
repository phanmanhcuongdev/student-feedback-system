package com.ttcs.backend.application.port.in;

import com.ttcs.backend.adapter.in.web.dto.SurveyResponse;

import java.util.List;

public interface GetSurveyUseCase {
    SurveyResponse getSurveyById(Integer surveyId);
    List<SurveyResponse> getAllSurveys();
}
