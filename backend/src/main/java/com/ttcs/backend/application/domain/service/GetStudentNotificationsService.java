package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.domain.model.SurveyStatus;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsQuery;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsUseCase;
import com.ttcs.backend.application.port.in.resultview.MarkStudentNotificationReadUseCase;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationPageResult;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.application.port.out.LoadStudentNotificationPort;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.LoadedStudentNotificationPage;
import com.ttcs.backend.application.port.out.NotificationCreateCommand;
import com.ttcs.backend.application.port.out.SaveNotificationPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetStudentNotificationsService implements GetStudentNotificationsUseCase, MarkStudentNotificationReadUseCase {

    private static final long CLOSING_SOON_DAYS = 3;

    private final LoadSurveyPort loadSurveyPort;
    private final LoadSurveyRecipientPort loadSurveyRecipientPort;
    private final LoadStudentNotificationPort loadStudentNotificationPort;
    private final SaveNotificationPort saveNotificationPort;

    @Override
    public StudentNotificationPageResult getNotifications(GetStudentNotificationsQuery query, Integer studentId) {
        createMissingDeadlineReminders(studentId, LocalDateTime.now());

        int page = Math.max(query == null ? 0 : query.page(), 0);
        int size = Math.min(Math.max(query == null ? 6 : query.size(), 1), 100);
        boolean unreadOnly = query != null && query.unreadOnly();
        LoadedStudentNotificationPage result = loadStudentNotificationPort.loadPage(studentId, page, size, unreadOnly);

        return new StudentNotificationPageResult(
                result.items().stream()
                        .map(item -> new StudentNotificationResult(
                                item.id(),
                                item.type(),
                                item.title(),
                                item.message(),
                                item.surveyId(),
                                item.surveyTitle(),
                                item.actionLabel(),
                                item.eventAt(),
                                item.readAt() != null,
                                item.readAt()
                        ))
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.unreadCount()
        );
    }

    @Override
    public boolean markAsRead(Integer notificationId, Integer userId) {
        if (notificationId == null || userId == null) {
            return false;
        }
        return loadStudentNotificationPort.markAsRead(notificationId, userId, LocalDateTime.now());
    }

    @Override
    public int markAllAsRead(Integer userId) {
        if (userId == null) {
            return 0;
        }
        return loadStudentNotificationPort.markAllAsRead(userId, LocalDateTime.now());
    }

    private void createMissingDeadlineReminders(Integer studentId, LocalDateTime now) {
        for (SurveyRecipient recipient : loadSurveyRecipientPort.loadByStudentId(studentId)) {
            if (recipient.hasSubmitted()) {
                continue;
            }

            Survey survey = loadSurveyPort.loadById(recipient.getSurveyId()).orElse(null);
            if (survey == null || !survey.isPublished() || survey.isHidden()) {
                continue;
            }

            SurveyStatus status = survey.statusAt(now);
            if (status != SurveyStatus.OPEN || !isWithinNextDays(survey.getEndDate(), now, CLOSING_SOON_DAYS)) {
                continue;
            }

            if (loadStudentNotificationPort.existsForUserAndSurvey(studentId, "SURVEY_DEADLINE_REMINDER", survey.getId())) {
                continue;
            }

            saveNotificationPort.create(new NotificationCreateCommand(
                    "SURVEY_DEADLINE_REMINDER",
                    "Survey closing soon",
                    "The survey \"" + survey.getTitle() + "\" is closing soon. Submit before the deadline.",
                    survey.getId(),
                    "Respond now",
                    null,
                    "deadline=" + safeEventAt(survey.getEndDate(), now),
                    List.of(studentId)
            ));
        }
    }

    private boolean isWithinNextDays(LocalDateTime value, LocalDateTime now, long days) {
        if (value == null || value.isBefore(now)) {
            return false;
        }

        return Duration.between(now, value).toDays() <= days;
    }

    private LocalDateTime safeEventAt(LocalDateTime preferred, LocalDateTime fallback) {
        return preferred != null ? preferred : fallback;
    }
}
