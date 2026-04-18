package com.ttcs.backend.application.port.in.resultview;

import com.ttcs.backend.application.domain.model.Role;

public interface GetSurveyResultListUseCase {
    SurveyResultPageResult getSurveyResults(GetSurveyResultsQuery query, Integer viewerUserId, Role viewerRole);
}
