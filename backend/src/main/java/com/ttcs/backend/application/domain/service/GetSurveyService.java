package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.port.in.GetSurveyUseCase;
import com.ttcs.backend.application.port.in.result.SurveySummaryResult;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@UseCase
public class GetSurveyService implements GetSurveyUseCase {

    private final LoadSurveyPort loadSurveyPort;

    @Override
    public SurveySummaryResult getSurveyById(Integer surveyId) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));

        return new SurveySummaryResult(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedBy(),
                survey.status()
        );
    }

    @Override
    public List<SurveySummaryResult> getAllSurveys() {
        return loadSurveyPort.loadAll()
                .stream()
                .map(survey -> new SurveySummaryResult(
                        survey.getId(),
                        survey.getTitle(),
                        survey.getDescription(),
                        survey.getStartDate(),
                        survey.getEndDate(),
                        survey.getCreatedBy(),
                        survey.status()
                ))
                .toList();
    }
}
