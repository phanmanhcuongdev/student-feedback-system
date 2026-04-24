package com.ttcs.backend.application.port.out;

import java.util.Optional;

public interface LoadSurveyResultPort {
    SurveyResultSearchPage loadPage(LoadSurveyResultsQuery query, String targetLang);

    Optional<SurveyResultDetail> loadSurveyResult(Integer surveyId, String targetLang);
}
