package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyRequest;
import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyDetailResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResponse;
import com.ttcs.backend.application.port.in.GetSurveyDetailUseCase;
import com.ttcs.backend.application.port.in.GetSurveyUseCase;
import com.ttcs.backend.application.port.in.SubmitSurveyUseCase;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@WebAdapter
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SurveyController {

    private final GetSurveyUseCase getSurveyUseCase;
    private final GetSurveyDetailUseCase getSurveyDetailUseCase;
    private final SubmitSurveyUseCase submitSurveyUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponse> getSurveyById(@PathVariable("id") Integer surveyId) {
        return ResponseEntity.ok(getSurveyUseCase.getSurveyById(surveyId));
    }

    @GetMapping
    public ResponseEntity<List<SurveyResponse>> getAllSurveys() {
        return ResponseEntity.ok(getSurveyUseCase.getAllSurveys());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<SurveyDetailResponse> getSurveyDetail(@PathVariable("id") Integer surveyId) {
        return ResponseEntity.ok(getSurveyDetailUseCase.getSurveyDetail(surveyId));
    }

    @PostMapping("/{surveyId}/submit")
    public SubmitSurveyResponse submitSurvey(
            @PathVariable Integer surveyId,
            @RequestBody SubmitSurveyRequest request
    ) {
        return submitSurveyUseCase.submitSurvey(surveyId, request);
    }


}
