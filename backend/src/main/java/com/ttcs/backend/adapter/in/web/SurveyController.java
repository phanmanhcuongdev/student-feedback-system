package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.SurveyResponse;
import com.ttcs.backend.application.port.in.GetSurveyUseCase;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@WebAdapter
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final GetSurveyUseCase getSurveyUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponse> getSurveyById(@PathVariable("id") Integer surveyId) {
        return ResponseEntity.ok(getSurveyUseCase.getSurveyById(surveyId));
    }
}
