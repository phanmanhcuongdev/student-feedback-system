package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import java.util.Optional;

public interface LoadSurveyResultPort {
    SurveyResultSearchPage loadPage(LoadSurveyResultsQuery query);

    Optional<SurveyResultDetailResult> loadSurveyResult(Integer surveyId);
}
