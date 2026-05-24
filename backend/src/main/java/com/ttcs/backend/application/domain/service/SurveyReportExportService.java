package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.in.resultview.ExportSurveyReportUseCase;
import com.ttcs.backend.application.domain.model.Lecturer;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.port.out.LoadLecturerByUserIdPort;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyReportPort;
import com.ttcs.backend.application.port.out.RenderedReport;
import com.ttcs.backend.application.port.out.EnterpriseSurveyReport;
import com.ttcs.backend.application.port.out.SurveyReportRenderer;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class SurveyReportExportService implements ExportSurveyReportUseCase {

    private final LoadSurveyReportPort loadSurveyReportPort;
    private final SurveyReportRenderer surveyReportRenderer;
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort;
    private final LoadLecturerByUserIdPort loadLecturerByUserIdPort;

    @Override
    @Transactional(readOnly = true)
    public ExportedReport exportSurveyReport(Integer surveyId, Integer viewerUserId, Role viewerRole, String format) {
        if (viewerRole != Role.ADMIN && viewerRole != Role.LECTURER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins and lecturers can export survey reports");
        }

        if (viewerRole == Role.LECTURER) {
            Lecturer lecturer = loadLecturerByUserIdPort.loadByUserId(viewerUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Lecturer profile not found"));
            
            Integer lecturerDepartmentId = lecturer.getDepartment() != null ? lecturer.getDepartment().getId() : null;
            if (lecturerDepartmentId == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lecturer department scope is unavailable");
            }

            List<SurveyAssignment> assignments = loadSurveyAssignmentPort.loadBySurveyId(surveyId);

            boolean isAuthorized = assignments.stream().anyMatch(a -> 
                    a.getSubjectType() == SubjectType.ALL ||
                    a.getSubjectType() == SubjectType.FACILITY ||
                    (a.getSubjectType() == SubjectType.DEPARTMENT && lecturerDepartmentId.equals(a.getSubjectValue())) ||
                    (a.getEvaluatorValue() == null) ||
                    lecturerDepartmentId.equals(a.getEvaluatorValue())
            );

            if (!isAuthorized) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Outside department scope");
            }
        }

        EnterpriseSurveyReport report = loadSurveyReportPort.loadSurveyReport(surveyId, viewerUserId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        return toExportedReport(surveyReportRenderer.render(report, format));
    }

    private ExportedReport toExportedReport(RenderedReport report) {
        return new ExportedReport(report.filename(), report.contentType(), report.content());
    }
}
