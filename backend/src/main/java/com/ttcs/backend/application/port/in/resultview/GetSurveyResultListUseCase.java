package com.ttcs.backend.application.port.in.resultview;

import com.ttcs.backend.application.domain.model.Role;

import java.util.List;

public interface GetSurveyResultListUseCase {
    List<SurveyResultSummaryResult> getSurveyResults(Integer viewerUserId, Role viewerRole);
}
