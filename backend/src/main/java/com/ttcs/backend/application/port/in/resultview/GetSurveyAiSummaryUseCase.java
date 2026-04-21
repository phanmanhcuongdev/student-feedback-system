package com.ttcs.backend.application.port.in.resultview;

import com.ttcs.backend.application.domain.model.Role;

public interface GetSurveyAiSummaryUseCase {
    SurveyAiSummaryViewResult getSummary(Integer surveyId, Integer viewerUserId, Role viewerRole);
}
