package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultSummaryResult;

import java.util.List;
import java.util.Optional;

public interface LoadSurveyResultPort {
    List<SurveyResultSummaryResult> loadSurveyResults();

    Optional<SurveyResultDetailResult> loadSurveyResult(Integer surveyId);
}
