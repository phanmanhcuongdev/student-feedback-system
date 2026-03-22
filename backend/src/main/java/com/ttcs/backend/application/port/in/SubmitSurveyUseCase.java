package com.ttcs.backend.application.port.in;

import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyRequest;
import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyResponse;

public interface SubmitSurveyUseCase {
    SubmitSurveyResponse submitSurvey(Integer surveyId, SubmitSurveyRequest request);
}
