package com.ttcs.backend.application.port.out;

import java.util.Optional;

public interface LoadSurveyReportPort {

    Optional<EnterpriseSurveyReport> loadSurveyReport(Integer surveyId, Integer generatedByUserId);
}
