package com.ttcs.backend.application.port.out;

import java.util.Optional;

public interface LoadSurveyReportPort {

    Optional<SurveyReport> loadSurveyReport(Integer surveyId);
}
