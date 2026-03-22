package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyAnswerRequest;
import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyRequest;
import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyResponse;
import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyResponseCode;
import com.ttcs.backend.application.domain.model.*;
import com.ttcs.backend.application.port.in.SubmitSurveyUseCase;
import com.ttcs.backend.application.port.out.*;
import com.ttcs.backend.common.UseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional
public class SubmitSurveyService implements SubmitSurveyUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final LoadStudentPort loadStudentPort;
    private final LoadQuestionPort loadQuestionPort;
    private final LoadSurveyResponsePort loadSurveyResponsePort;
    private final SaveSurveyResponsePort saveSurveyResponsePort;
    private final SaveResponseDetailPort saveResponseDetailPort;

    @Override
    public SubmitSurveyResponse submitSurvey(Integer surveyId, SubmitSurveyRequest request) {
        if (request == null
                || request.getStudentId() == null
                || request.getAnswers() == null
                || request.getAnswers().isEmpty()) {
            return SubmitSurveyResponse.fail(
                    SubmitSurveyResponseCode.INVALID_INPUT,
                    "Invalid submit request"
            );
        }

        boolean alreadySubmitted =
                loadSurveyResponsePort.existsBySurveyIdAndStudentId(surveyId, request.getStudentId());

        if (alreadySubmitted) {
            return SubmitSurveyResponse.fail(
                    SubmitSurveyResponseCode.ALREADY_SUBMITTED,
                    "Student has already submitted this survey"
            );
        }

        Survey survey = loadSurveyPort.loadById(surveyId).orElse(null);
        if (survey == null) {
            return SubmitSurveyResponse.fail(
                    SubmitSurveyResponseCode.SURVEY_NOT_FOUND,
                    "Survey not found"
            );
        }

        Student student = loadStudentPort.loadById(request.getStudentId()).orElse(null);
        if (student == null) {
            return SubmitSurveyResponse.fail(
                    SubmitSurveyResponseCode.INVALID_INPUT,
                    "Student not found"
            );
        }

        SurveyResponse surveyResponse = new SurveyResponse(
                null,
                student,
                null,
                survey,
                LocalDateTime.now()
        );

        SurveyResponse savedSurveyResponse = saveSurveyResponsePort.save(surveyResponse);

        List<Question> surveyQuestions = loadQuestionPort.loadBySurveyId(surveyId);
        List<ResponseDetail> responseDetails = new ArrayList<>();

        for (SubmitSurveyAnswerRequest answerRequest : request.getAnswers()) {
            if (answerRequest.getQuestionId() == null) {
                return SubmitSurveyResponse.fail(
                        SubmitSurveyResponseCode.INVALID_INPUT,
                        "Question id is required"
                );
            }

            Question matchedQuestion = null;
            for (Question question : surveyQuestions) {
                if (question.getId().equals(answerRequest.getQuestionId())) {
                    matchedQuestion = question;
                    break;
                }
            }

            if (matchedQuestion == null) {
                return SubmitSurveyResponse.fail(
                        SubmitSurveyResponseCode.INVALID_INPUT,
                        "Question does not belong to this survey"
                );
            }

            ResponseDetail responseDetail = new ResponseDetail(
                    null,
                    savedSurveyResponse,
                    matchedQuestion,
                    answerRequest.getRating(),
                    answerRequest.getComment()
            );

            responseDetails.add(responseDetail);
        }

        saveResponseDetailPort.saveAll(responseDetails);

        return SubmitSurveyResponse.success("Submit survey successfully");
    }
}