package com.ttcs.backend.application.port.in.resultview;

import java.util.List;

public interface GetSurveyResultListUseCase {
    List<SurveyResultSummaryResult> getSurveyResults();
}
