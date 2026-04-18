package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.port.in.GetStudentSurveysQuery;
import com.ttcs.backend.application.port.in.GetSurveyUseCase;
import com.ttcs.backend.application.port.in.result.StudentSurveyPageResult;
import com.ttcs.backend.application.port.in.result.SurveySummaryResult;
import com.ttcs.backend.application.port.out.LoadStudentSurveysQuery;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.SaveSurveyRecipientPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
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
    public StudentSurveyPageResult getAllSurveys(GetStudentSurveysQuery query, Integer studentUserId) {
        Student student = loadStudentByIdPort.loadByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        var page = loadSurveyPort.loadStudentSurveyPage(new LoadStudentSurveysQuery(
                student.getId(),
                query == null ? null : query.status(),
                query == null ? 0 : query.page(),
                query == null ? 12 : query.size(),
                query == null ? "endDate" : query.sortBy(),
                query == null ? "asc" : query.sortDir()
        ));
        return new StudentSurveyPageResult(
                page.items().stream().map(item -> new SurveySummaryResult(
                        item.id(),
                        item.title(),
                        item.description(),
                        item.startDate(),
                        item.endDate(),
                        item.createdBy(),
                        item.status()
                )).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
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
