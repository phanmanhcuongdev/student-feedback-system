package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.adapter.in.web.dto.QuestionItemResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyDetailResponse;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.port.in.GetSurveyDetailUseCase;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetSurveyDetailService implements GetSurveyDetailUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final LoadQuestionPort loadQuestionPort;

    @Override
    public SurveyDetailResponse getSurveyDetail(Integer surveyId) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        List<Question> questions = loadQuestionPort.loadBySurveyId(surveyId);

        String status;
        LocalDateTime now = LocalDateTime.now();

        if (survey.getStartDate().isAfter(now)) {
            status = "NOT_OPEN";
        } else if (survey.getEndDate().isBefore(now)) {
            status = "CLOSED";
        } else {
            status = "OPEN";
        }

        List<QuestionItemResponse> questionItems = questions.stream()
                .map(q -> new QuestionItemResponse(
                        q.getId(),
                        q.getContent(),
                        q.getType().name()
                ))
                .toList();

        return new SurveyDetailResponse(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                status,
                questionItems
        );
    }
}