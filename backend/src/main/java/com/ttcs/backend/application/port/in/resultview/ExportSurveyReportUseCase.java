package com.ttcs.backend.application.port.in.resultview;

import com.ttcs.backend.application.domain.model.Role;

public interface ExportSurveyReportUseCase {

    ExportedReport exportSurveyReport(Integer surveyId, Integer viewerUserId, Role viewerRole, String format);
}
