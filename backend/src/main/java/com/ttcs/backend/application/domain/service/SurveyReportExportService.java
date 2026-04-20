package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.in.resultview.ExportSurveyReportUseCase;
import com.ttcs.backend.application.port.in.resultview.SurveyReportQuestionView;
import com.ttcs.backend.application.port.in.resultview.SurveyReportRatingBreakdownView;
import com.ttcs.backend.application.port.in.resultview.SurveyReportView;
import com.ttcs.backend.application.port.out.LoadSurveyReportPort;
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
        return surveyReportRenderer.render(toView(report));
    }

    private SurveyReportView toView(SurveyReport report) {
        return new SurveyReportView(
                report.id(),
                report.title(),
                report.description(),
                report.startDate(),
                report.endDate(),
                report.lifecycleState(),
                report.runtimeStatus(),
                report.recipientScope(),
                report.recipientDepartmentName(),
                report.targetedCount(),
                report.openedCount(),
                report.submittedCount(),
                report.responseRate(),
                report.questions().stream()
                        .map(question -> new SurveyReportQuestionView(
                                question.id(),
                                question.content(),
                                question.type(),
                                question.responseCount(),
                                question.averageRating(),
                                question.ratingBreakdown().stream()
                                        .map(item -> new SurveyReportRatingBreakdownView(item.rating(), item.count()))
                                        .toList(),
                                question.comments()
                        ))
                        .toList()
        );
    }
}
