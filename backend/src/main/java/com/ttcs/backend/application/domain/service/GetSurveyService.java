package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.adapter.in.web.dto.SurveyResponse;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.port.in.GetSurveyUseCase;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@UseCase
public class GetSurveyService implements GetSurveyUseCase {

    private final LoadSurveyPort loadSurveyPort;

    @Override
    public SurveyResponse getSurveyById(Integer surveyId) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        String status;
        if (survey.isNotStarted()) {
            status = "NOT_OPEN";
        } else if (survey.isClosed()) {
            status = "CLOSED";
        } else {
            status = "OPEN";
        }

        return new SurveyResponse(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedBy(),
                status
        );
    }

    @Override
    public List<SurveyResponse> getAllSurveys() {
        return loadSurveyPort.loadAll()
                .stream()
                .map(survey -> {
                    String status;
                    if (survey.isNotStarted()) {
                        status = "NOT_OPEN";
                    } else if (survey.isClosed()) {
                        status = "CLOSED";
                    } else {
                        status = "OPEN";
                    }

                    return new SurveyResponse(
                            survey.getId(),
                            survey.getTitle(),
                            survey.getDescription(),
                            survey.getStartDate(),
                            survey.getEndDate(),
                            survey.getCreatedBy(),
                            status
                    );
                })
                .toList();
    }

}
