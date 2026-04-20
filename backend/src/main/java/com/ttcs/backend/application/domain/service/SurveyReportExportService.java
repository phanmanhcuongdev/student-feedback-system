package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.in.resultview.ExportSurveyReportUseCase;
import com.ttcs.backend.application.port.out.LoadSurveyReportPort;
import com.ttcs.backend.application.port.out.RenderedReport;
import com.ttcs.backend.application.port.out.SurveyReport;
import com.ttcs.backend.application.port.out.SurveyReportRenderer;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@UseCase
@RequiredArgsConstructor
public class SurveyReportExportService implements ExportSurveyReportUseCase {

    private final LoadSurveyReportPort loadSurveyReportPort;
    private final SurveyReportRenderer surveyReportRenderer;

    @Override
    @Transactional(readOnly = true)
    public ExportedReport exportSurveyReport(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        if (viewerRole != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can export survey reports");
        }

        SurveyReport report = loadSurveyReportPort.loadSurveyReport(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        return toExportedReport(surveyReportRenderer.render(report));
    }

    private ExportedReport toExportedReport(RenderedReport report) {
        return new ExportedReport(report.filename(), report.contentType(), report.content());
    }
}
