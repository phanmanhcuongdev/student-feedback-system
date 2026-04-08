package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.port.in.GetSurveyDetailUseCase;
import com.ttcs.backend.application.port.in.result.QuestionItemResult;
import com.ttcs.backend.application.port.in.result.SurveyDetailResult;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetSurveyDetailService implements GetSurveyDetailUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final LoadQuestionPort loadQuestionPort;

    @Override
    public SurveyDetailResult getSurveyDetail(Integer surveyId) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));

        List<Question> questions = loadQuestionPort.loadBySurveyId(surveyId);

        List<QuestionItemResult> questionItems = questions.stream()
                .map(q -> new QuestionItemResult(
                        q.getId(),
                        q.getContent(),
                        q.getType()
                ))
                .toList();

        return new SurveyDetailResult(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.status(),
                questionItems
        );
    }
}
