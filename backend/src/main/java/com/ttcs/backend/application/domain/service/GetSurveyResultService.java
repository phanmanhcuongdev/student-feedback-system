package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.EvaluatorType;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.SubjectType;
import com.ttcs.backend.application.domain.model.SurveyAssignment;
import com.ttcs.backend.application.domain.model.Lecturer;
import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultDetailUseCase;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultsQuery;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultListUseCase;
import com.ttcs.backend.application.port.in.resultview.QuestionStatisticsResult;
import com.ttcs.backend.application.port.in.resultview.RatingBreakdownResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultMetricsResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultPageResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultSummaryResult;
import com.ttcs.backend.application.port.out.LoadSurveyResultsQuery;
import com.ttcs.backend.application.port.out.LoadSurveyAssignmentPort;
import com.ttcs.backend.application.port.out.LoadSurveyResultPort;
import com.ttcs.backend.application.port.out.LoadLecturerByUserIdPort;
import com.ttcs.backend.application.port.out.QuestionStatistics;
import com.ttcs.backend.application.port.out.SurveyResultDetail;
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
    private final LoadLecturerByUserIdPort loadLecturerByUserIdPort;

    @Override
    public SurveyResultPageResult getSurveyResults(GetSurveyResultsQuery query, Integer viewerUserId, Role viewerRole) {
        Integer lecturerDepartmentId = null;
        if (viewerRole != Role.ADMIN) {
            Lecturer lecturer = requireLecturer(viewerUserId, viewerRole);
            lecturerDepartmentId = lecturer.getDepartment() != null ? lecturer.getDepartment().getId() : null;
            if (lecturerDepartmentId == null) {
                throw new ResponseStatusException(FORBIDDEN, "Lecturer department scope is unavailable");
            }
        }

        var page = loadSurveyResultPort.loadPage(new LoadSurveyResultsQuery(
                query == null ? null : query.keyword(),
                query == null ? null : query.lifecycleState(),
                query == null ? null : query.runtimeStatus(),
                query == null ? null : query.recipientScope(),
                query == null ? null : query.startDateFrom(),
                query == null ? null : query.endDateTo(),
                lecturerDepartmentId,
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
        SurveyResultDetail result = loadSurveyResultPort.loadSurveyResult(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));

        if (viewerRole == Role.ADMIN) {
            return toDetailResult(result);
        }

        Lecturer lecturer = requireLecturer(viewerUserId, viewerRole);
        Integer lecturerDepartmentId = lecturer.getDepartment() != null ? lecturer.getDepartment().getId() : null;
        if (lecturerDepartmentId == null) {
            throw new ResponseStatusException(FORBIDDEN, "Lecturer department scope is unavailable");
        }
        if (!isLecturerInScope(surveyId, lecturerDepartmentId)) {
            throw new ResponseStatusException(FORBIDDEN, "You are not allowed to view results for this survey");
        }

        return toDetailResult(result);
    }

    private SurveyResultDetailResult toDetailResult(SurveyResultDetail result) {
        return new SurveyResultDetailResult(
                result.id(),
                result.title(),
                result.description(),
                result.startDate(),
                result.endDate(),
                result.status(),
                result.lifecycleState(),
                result.runtimeStatus(),
                result.recipientScope(),
                result.recipientDepartmentName(),
                result.responseCount(),
                result.targetedCount(),
                result.openedCount(),
                result.submittedCount(),
                result.responseRate(),
                result.questions().stream()
                        .map(this::toQuestionStatisticsResult)
                        .toList()
        );
    }

    private QuestionStatisticsResult toQuestionStatisticsResult(QuestionStatistics question) {
        return new QuestionStatisticsResult(
                question.id(),
                question.content(),
                question.type(),
                question.responseCount(),
                question.averageRating(),
                question.ratingBreakdown().stream()
                        .map(item -> new RatingBreakdownResult(item.rating(), item.count()))
                        .toList(),
                question.comments()
        );
    }

    private Lecturer requireLecturer(Integer viewerUserId, Role viewerRole) {
        if (viewerUserId == null || viewerRole != Role.LECTURER) {
            throw new ResponseStatusException(FORBIDDEN, "You are not allowed to view survey results");
        }

        return loadLecturerByUserIdPort.loadByUserId(viewerUserId)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Lecturer profile not found"));
    }

    private boolean isLecturerInScope(Integer surveyId, Integer lecturerDepartmentId) {
        List<SurveyAssignment> assignments = loadSurveyAssignmentPort.loadBySurveyId(surveyId);
        return assignments.stream().anyMatch(assignment ->
                assignment != null
                        && assignment.getEvaluatorType() == EvaluatorType.STUDENT
                        && assignment.getSubjectType() == SubjectType.DEPARTMENT
                        && lecturerDepartmentId.equals(assignment.getSubjectValue())
        );
    }
}
