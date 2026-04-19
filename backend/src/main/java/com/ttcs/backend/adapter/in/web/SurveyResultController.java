package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.QuestionStatisticsResponse;
import com.ttcs.backend.adapter.in.web.dto.RatingBreakdownResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultDetailResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultMetricsResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultPageResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultSummaryResponse;
import com.ttcs.backend.application.domain.model.Role;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@WebAdapter
@RequestMapping("/api/v1/survey-results")
@RequiredArgsConstructor
public class SurveyResultController {

    private final GetSurveyResultListUseCase getSurveyResultListUseCase;
    private final GetSurveyResultDetailUseCase getSurveyResultDetailUseCase;
    private final CurrentStudentProvider currentStudentProvider;

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
                currentStudentProvider.currentUserId(),
                currentStudentProvider.currentRole()
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
                        currentStudentProvider.currentUserId(),
                        currentStudentProvider.currentRole()
                )
        ));
    }

    @GetMapping("/{surveyId}/export")
    public ResponseEntity<byte[]> exportSurveyResult(@PathVariable Integer surveyId) {
        if (currentStudentProvider.currentRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can export survey reports");
        }

        SurveyResultDetailResult result = getSurveyResultDetailUseCase.getSurveyResult(
                surveyId,
                currentStudentProvider.currentUserId(),
                currentStudentProvider.currentRole()
        );
        byte[] csv = buildCsv(result).getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("survey-" + surveyId + "-report.csv")
                .build());
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
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

    private String buildCsv(SurveyResultDetailResult result) {
        StringBuilder csv = new StringBuilder();
        appendRow(csv,
                "Survey ID",
                "Survey Title",
                "Description",
                "Start Date",
                "End Date",
                "Lifecycle State",
                "Runtime Status",
                "Audience",
                "Department",
                "Targeted",
                "Opened",
                "Submitted",
                "Response Rate",
                "Question ID",
                "Question",
                "Question Type",
                "Question Response Count",
                "Average Rating",
                "Rating",
                "Rating Count",
                "Comment"
        );
        for (QuestionStatisticsResult question : result.questions()) {
            if ("RATING".equalsIgnoreCase(question.type())) {
                if (question.ratingBreakdown().isEmpty()) {
                    appendQuestionRow(csv, result, question, null, null, null);
                } else {
                    question.ratingBreakdown().forEach(item -> appendQuestionRow(csv, result, question, item.rating(), item.count(), null));
                }
            } else if (question.comments().isEmpty()) {
                appendQuestionRow(csv, result, question, null, null, null);
            } else {
                question.comments().forEach(comment -> appendQuestionRow(csv, result, question, null, null, comment));
            }
        }
        return csv.toString();
    }

    private void appendQuestionRow(
            StringBuilder csv,
            SurveyResultDetailResult result,
            QuestionStatisticsResult question,
            Integer rating,
            Long ratingCount,
            String comment
    ) {
        appendRow(csv,
                result.id(),
                result.title(),
                result.description(),
                result.startDate(),
                result.endDate(),
                result.lifecycleState(),
                result.runtimeStatus(),
                result.recipientScope(),
                result.recipientDepartmentName(),
                result.targetedCount(),
                result.openedCount(),
                result.submittedCount(),
                result.responseRate(),
                question.id(),
                question.content(),
                question.type(),
                question.responseCount(),
                question.averageRating(),
                rating,
                ratingCount,
                comment
        );
    }

    private void appendRow(StringBuilder csv, Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escapeCsv(values[i]));
        }
        csv.append('\n');
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
