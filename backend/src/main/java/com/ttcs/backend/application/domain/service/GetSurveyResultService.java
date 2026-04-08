package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultDetailUseCase;
import com.ttcs.backend.application.port.in.resultview.GetSurveyResultListUseCase;
import com.ttcs.backend.application.port.in.resultview.SurveyResultDetailResult;
import com.ttcs.backend.application.port.in.resultview.SurveyResultSummaryResult;
import com.ttcs.backend.application.port.out.LoadSurveyResultPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetSurveyResultService implements GetSurveyResultListUseCase, GetSurveyResultDetailUseCase {

    private final LoadSurveyResultPort loadSurveyResultPort;

    @Override
    public List<SurveyResultSummaryResult> getSurveyResults() {
        return loadSurveyResultPort.loadSurveyResults();
    }

    @Override
    public SurveyResultDetailResult getSurveyResult(Integer surveyId) {
        return loadSurveyResultPort.loadSurveyResult(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
    }
}
