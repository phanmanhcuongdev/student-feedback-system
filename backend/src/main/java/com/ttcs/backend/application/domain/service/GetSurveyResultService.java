package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.Teacher;
import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultDetailUseCase;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultsQuery;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultListUseCase;
import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultMetricsResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultPageResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultSummaryResult;
import com.ttcs.backend.application.port.out.LoadSurveyResultsQuery;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyResultPort;
import com.ttcs.backend.application.port.out.LoadTeacherByUserIdPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@UseCase
@RequiredArgsConstructor
public class GetSurveyResultService implements GetSurveyResultListUseCase, GetSurveyResultDetailUseCase {

    private final LoadSurveyResultPort loadSurveyResultPort;
    private final LoadSurveyAssignmentPort loadSurveyAssignmentPort;
    private final LoadTeacherByUserIdPort loadTeacherByUserIdPort;

    @Override
    public SurveyResultPageResult getSurveyResults(GetSurveyResultsQuery query, Integer viewerUserId, Role viewerRole) {
        Integer teacherDepartmentId = null;
        if (viewerRole != Role.ADMIN) {
            Teacher teacher = requireTeacher(viewerUserId, viewerRole);
            teacherDepartmentId = teacher.getDepartment() != null ? teacher.getDepartment().getId() : null;
            if (teacherDepartmentId == null) {
                throw new ResponseStatusException(FORBIDDEN, "Teacher department scope is unavailable");
            }
        }

        var page = loadSurveyResultPort.loadPage(new LoadSurveyResultsQuery(
                query == null ? null : query.keyword(),
                query == null ? null : query.lifecycleState(),
                query == null ? null : query.runtimeStatus(),
                query == null ? null : query.recipientScope(),
                query == null ? null : query.startDateFrom(),
                query == null ? null : query.endDateTo(),
                teacherDepartmentId,
                query == null ? 0 : query.page(),
                query == null ? 12 : query.size(),
                query == null ? "responseRate" : query.sortBy(),
                query == null ? "desc" : query.sortDir()
        ));

        return new SurveyResultPageResult(
                page.items().stream()
                        .map(item -> new SurveyResultSummaryResult(
                                item.id(),
                                item.title(),
                                item.description(),
                                item.startDate(),
                                item.endDate(),
                                item.status(),
                                item.lifecycleState(),
                                item.runtimeStatus(),
                                item.recipientScope(),
                                item.recipientDepartmentName(),
                                item.responseCount(),
                                item.targetedCount(),
                                item.openedCount(),
                                item.submittedCount(),
                                item.responseRate()
                        ))
                        .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                new SurveyResultMetricsResult(
                        page.metrics().total(),
                        page.metrics().open(),
                        page.metrics().closed(),
                        page.metrics().averageResponseRate(),
                        page.metrics().totalSubmitted(),
                        page.metrics().totalResponses()
                )
        );
    }

    @Override
    public SurveyResultDetailResult getSurveyResult(Integer surveyId, Integer viewerUserId, Role viewerRole) {
        SurveyResultDetailResult result = loadSurveyResultPort.loadSurveyResult(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));

        if (viewerRole == Role.ADMIN) {
            return result;
        }

        Teacher teacher = requireTeacher(viewerUserId, viewerRole);
        Integer teacherDepartmentId = teacher.getDepartment() != null ? teacher.getDepartment().getId() : null;
        if (teacherDepartmentId == null) {
            throw new ResponseStatusException(FORBIDDEN, "Teacher department scope is unavailable");
        }
        if (!isTeacherInScope(surveyId, teacherDepartmentId)) {
            throw new ResponseStatusException(FORBIDDEN, "You are not allowed to view results for this survey");
        }

        return result;
    }

    private Teacher requireTeacher(Integer viewerUserId, Role viewerRole) {
        if (viewerUserId == null || viewerRole != Role.TEACHER) {
            throw new ResponseStatusException(FORBIDDEN, "You are not allowed to view survey results");
        }

        return loadTeacherByUserIdPort.loadByUserId(viewerUserId)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Teacher profile not found"));
    }

    private boolean isTeacherInScope(Integer surveyId, Integer teacherDepartmentId) {
        List<SurveyAssignment> assignments = loadSurveyAssignmentPort.loadBySurveyId(surveyId);
        return assignments.stream().anyMatch(assignment ->
                assignment != null
                        && assignment.getEvaluatorType() == EvaluatorType.STUDENT
                        && assignment.getSubjectType() == SubjectType.DEPARTMENT
                        && teacherDepartmentId.equals(assignment.getSubjectValue())
        );
    }
}
