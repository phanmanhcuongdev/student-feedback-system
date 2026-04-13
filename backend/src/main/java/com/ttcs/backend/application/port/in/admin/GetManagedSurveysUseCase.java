package com.ttcs.backend.application.port.in.admin;

import java.util.List;

public interface GetManagedSurveysUseCase {
    List<SurveyManagementSummaryResult> getSurveys();
}
