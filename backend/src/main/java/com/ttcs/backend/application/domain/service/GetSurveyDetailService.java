package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.port.in.GetSurveyDetailUseCase;
import com.ttcs.backend.application.port.in.result.QuestionItemResult;
import com.ttcs.backend.application.port.in.result.SurveyDetailResult;
import com.ttcs.backend.application.port.out.LoadQuestionPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.SaveSurveyRecipientPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetSurveyDetailService implements GetSurveyDetailUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final LoadQuestionPort loadQuestionPort;
    private final LoadSurveyRecipientPort loadSurveyRecipientPort;
    private final SaveSurveyRecipientPort saveSurveyRecipientPort;

    @Override
    public SurveyDetailResult getSurveyDetail(Integer surveyId, Integer studentId) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        SurveyRecipient recipient = loadSurveyRecipientPort.loadBySurveyIdAndStudentId(surveyId, studentId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));

        if (!survey.isPublished() || survey.isHidden()) {
            throw new SurveyNotFoundException(surveyId);
        }
        if (!recipient.hasOpened()) {
            saveSurveyRecipientPort.save(new SurveyRecipient(
                    recipient.getId(),
                    recipient.getSurveyId(),
                    recipient.getStudentId(),
                    recipient.getAssignedAt(),
                    LocalDateTime.now(),
                    recipient.getSubmittedAt()
            ));
        }

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
