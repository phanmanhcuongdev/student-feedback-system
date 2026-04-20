package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.QuestionStatisticsResponse;
import com.ttcs.backend.adapter.in.web.dto.RatingBreakdownResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultDetailResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultMetricsResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultPageResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultSummaryResponse;
import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.in.resultview.ExportSurveyReportUseCase;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultDetailUseCase;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultsQuery;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultListUseCase;
import com.ttcs.backend.application.port.in.resultview.QuestionStatisticsResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultPageResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultSummaryResult;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@WebAdapter
@RequestMapping("/api/v1/survey-results")
@RequiredArgsConstructor
public class SurveyResultController {

    private final GetSurveyResultListUseCase getSurveyResultListUseCase;
    private final GetSurveyResultDetailUseCase getSurveyResultDetailUseCase;
    private final ExportSurveyReportUseCase exportSurveyReportUseCase;
    private final CurrentIdentityProvider currentIdentityProvider;

    @GetMapping
    public ResponseEntity<SurveyResultPageResponse> getSurveyResults(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lifecycleState,
            @RequestParam(required = false) String runtimeStatus,
            @RequestParam(required = false) String recipientScope,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "responseRate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        SurveyResultPageResult result = getSurveyResultListUseCase.getSurveyResults(
                new GetSurveyResultsQuery(
                        keyword,
                        lifecycleState,
                        runtimeStatus,
                        recipientScope,
                        startDateFrom,
                        endDateTo,
                        page,
                        size,
                        sortBy,
                        sortDir
                ),
                currentIdentityProvider.currentUserId(),
                currentIdentityProvider.currentRole()
        );
        return ResponseEntity.ok(new SurveyResultPageResponse(
                result.items().stream().map(this::toSummaryResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                new SurveyResultMetricsResponse(
                        result.metrics().total(),
                        result.metrics().open(),
                        result.metrics().closed(),
                        result.metrics().averageResponseRate(),
                        result.metrics().totalSubmitted(),
                        result.metrics().totalResponses()
                )
        ));
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyResultDetailResponse> getSurveyResult(@PathVariable Integer surveyId) {
        return ResponseEntity.ok(toDetailResponse(
                getSurveyResultDetailUseCase.getSurveyResult(
                        surveyId,
                        currentIdentityProvider.currentUserId(),
                        currentIdentityProvider.currentRole()
                )
        ));
    }

    @GetMapping("/{surveyId}/export")
    public ResponseEntity<byte[]> exportSurveyResult(@PathVariable Integer surveyId) {
        ExportedReport result = exportSurveyReportUseCase.exportSurveyReport(
                surveyId,
                currentIdentityProvider.currentUserId(),
                currentIdentityProvider.currentRole()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.contentType()));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(result.filename())
                .build());
        return new ResponseEntity<>(result.content(), headers, HttpStatus.OK);
    }

    private SurveyResultSummaryResponse toSummaryResponse(SurveyResultSummaryResult result) {
        return new SurveyResultSummaryResponse(
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
                result.responseRate()
        );
    }

    private SurveyResultDetailResponse toDetailResponse(SurveyResultDetailResult result) {
        return new SurveyResultDetailResponse(
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
                result.questions().stream().map(this::toQuestionResponse).toList()
        );
    }

    private QuestionStatisticsResponse toQuestionResponse(QuestionStatisticsResult result) {
        return new QuestionStatisticsResponse(
                result.id(),
                result.content(),
                result.type(),
                result.responseCount(),
                result.averageRating(),
                result.ratingBreakdown().stream()
                        .map(item -> new RatingBreakdownResponse(item.rating(), item.count()))
                        .toList(),
                result.comments()
        );
    }

}
