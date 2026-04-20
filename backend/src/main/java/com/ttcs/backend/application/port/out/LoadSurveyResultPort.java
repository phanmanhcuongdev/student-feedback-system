package com.ttcs.backend.application.port.out;

import java.util.Optional;

public interface LoadSurveyResultPort {
    SurveyResultSearchPage loadPage(LoadSurveyResultsQuery query);

    Optional<SurveyResultDetail> loadSurveyResult(Integer surveyId);
}
