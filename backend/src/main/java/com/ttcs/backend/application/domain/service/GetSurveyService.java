package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.domain.model.SurveyStatus;
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
import java.time.ZoneId;

@RequiredArgsConstructor
@UseCase
public class GetSurveyService implements GetSurveyUseCase {
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final LoadSurveyPort loadSurveyPort;
    private final LoadStudentByIdPort loadStudentByIdPort;
    private final LoadSurveyRecipientPort loadSurveyRecipientPort;
    private final SaveSurveyRecipientPort saveSurveyRecipientPort;

    @Override
    public SurveySummaryResult getSurveyById(Integer surveyId, Integer studentUserId, String targetLang) {
        Survey survey = loadSurveyPort.loadById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        Student student = loadStudentByIdPort.loadByUserId(studentUserId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        SurveyRecipient recipient = loadSurveyRecipientPort.loadBySurveyIdAndStudentId(surveyId, student.getId()).orElse(null);
        if (!survey.isPublished() || survey.isHidden() || isExpired(survey) || survey.status() == SurveyStatus.CLOSED || recipient == null) {
            throw new SurveyNotFoundException(surveyId);
        }
        markOpenedIfNecessary(recipient);

        return new SurveySummaryResult(
                survey.getId(),
                survey.displayTitle(targetLang),
                survey.displayDescription(targetLang),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getCreatedBy(),
                survey.status(),
                recipient.hasSubmitted()
        );
    }

    @Override
    public StudentSurveyPageResult getAllSurveys(GetStudentSurveysQuery query, Integer studentUserId, String targetLang) {
        Student student = loadStudentByIdPort.loadByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        var page = loadSurveyPort.loadStudentSurveyPage(new LoadStudentSurveysQuery(
                student.getId(),
                query == null ? null : query.status(),
                query == null ? null : query.submitted(),
                normalizeLanguage(targetLang),
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
                        item.status(),
                        item.submitted()
                )).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    private String normalizeLanguage(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "en";
        }
        return value.split(",")[0].trim().split("-")[0].toLowerCase();
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

    private boolean isExpired(Survey survey) {
        return survey.getEndDate() != null && !LocalDateTime.now(APP_ZONE).isBefore(survey.getEndDate());
    }
}
