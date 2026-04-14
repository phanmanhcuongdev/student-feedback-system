package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.QuestionStatisticsResponse;
import com.ttcs.backend.adapter.in.web.dto.RatingBreakdownResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultDetailResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResultSummaryResponse;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultDetailUseCase;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultListUseCase;
import com.ttcs.backend.application.port.in.resultview.QuestionStatisticsResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultSummaryResult;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@WebAdapter
@RequestMapping("/api/v1/survey-results")
@RequiredArgsConstructor
public class SurveyResultController {

    private final GetSurveyResultListUseCase getSurveyResultListUseCase;
    private final GetSurveyResultDetailUseCase getSurveyResultDetailUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @GetMapping
    public ResponseEntity<List<SurveyResultSummaryResponse>> getSurveyResults() {
        return ResponseEntity.ok(
                getSurveyResultListUseCase.getSurveyResults(
                                currentStudentProvider.currentUserId(),
                                currentStudentProvider.currentRole()
                        ).stream()
                        .map(this::toSummaryResponse)
                        .toList()
        );
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

    private SurveyResultSummaryResponse toSummaryResponse(SurveyResultSummaryResult result) {
        return new SurveyResultSummaryResponse(
                result.id(),
                result.title(),
                result.description(),
                result.startDate(),
                result.endDate(),
                result.status(),
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
