package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.port.in.GetSurveyUseCase;
import com.ttcs.backend.application.port.in.result.SurveySummaryResult;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.SaveSurveyRecipientPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@UseCase
public class GetSurveyService implements GetSurveyUseCase {

    private final LoadSurveyPort loadSurveyPort;
    private final LoadStudentByIdPort loadStudentByIdPort;
    private final LoadSurveyRecipientPort loadSurveyRecipientPort;
    private final SaveSurveyRecipientPort saveSurveyRecipientPort;

    @Override
    public SurveySummaryResult getSurveyById(Integer surveyId, Integer studentUserId) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        Student student = loadStudentByIdPort.loadByUserId(studentUserId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        SurveyRecipient recipient = loadSurveyRecipientPort.loadBySurveyIdAndStudentId(surveyId, student.getId()).orElse(null);
        if (!survey.isPublished() || survey.isHidden() || recipient == null) {
            throw new SurveyNotFoundException(surveyId);
        }
        markOpenedIfNecessary(recipient);

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
    public List<SurveySummaryResult> getAllSurveys(Integer studentUserId) {
        Student student = loadStudentByIdPort.loadByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        return loadSurveyRecipientPort.loadByStudentId(student.getId()).stream()
                .map(recipient -> loadSurveyPort.loadById(recipient.getSurveyId()).orElse(null))
                .filter(survey -> survey != null && survey.isPublished() && !survey.isHidden())
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

    private void markOpenedIfNecessary(SurveyRecipient recipient) {
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
    }
}
