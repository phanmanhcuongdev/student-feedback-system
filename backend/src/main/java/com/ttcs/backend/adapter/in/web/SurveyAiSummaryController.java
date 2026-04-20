package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.SurveyAiSummaryResponse;
import com.ttcs.backend.application.port.in.resultview.GenerateSurveyAiSummaryUseCase;
import com.ttcs.backend.application.port.in.resultview.GetSurveyAiSummaryUseCase;
import com.ttcs.backend.application.port.in.resultview.SurveyAiSummaryViewResult;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequestMapping("/api/v1/survey-results/{surveyId}/ai-summary")
@RequiredArgsConstructor
public class SurveyAiSummaryController {

    private final GetSurveyAiSummaryUseCase getSurveyAiSummaryUseCase;
    private final GenerateSurveyAiSummaryUseCase generateSurveyAiSummaryUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @GetMapping
    public ResponseEntity<SurveyAiSummaryResponse> getSummary(@PathVariable Integer surveyId) {
        return ResponseEntity.ok(toResponse(
                getSurveyAiSummaryUseCase.getSummary(
                        surveyId,
                        currentStudentProvider.currentUserId(),
                        currentStudentProvider.currentRole()
                )
        ));
    }

    @PostMapping("/generate")
    public ResponseEntity<SurveyAiSummaryResponse> generate(@PathVariable Integer surveyId) {
        SurveyAiSummaryViewResult result = generateSurveyAiSummaryUseCase.generate(
                surveyId,
                currentStudentProvider.currentUserId(),
                currentStudentProvider.currentRole()
        );
        HttpStatus status = "COMPLETED".equals(result.status()) ? HttpStatus.OK : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(toResponse(result));
    }

    private SurveyAiSummaryResponse toResponse(SurveyAiSummaryViewResult result) {
        return new SurveyAiSummaryResponse(
                result.surveyId(),
                result.status(),
                result.jobId(),
                result.commentCount(),
                result.summary(),
                result.highlights(),
                result.concerns(),
                result.actions(),
                result.modelName(),
                result.errorMessage(),
                result.requestedAt(),
                result.startedAt(),
                result.finishedAt()
        );
    }
}
