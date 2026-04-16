package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.domain.model.SurveyStatus;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsUseCase;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetStudentNotificationsService implements GetStudentNotificationsUseCase {

    private static final long NEW_SURVEY_DAYS = 7;
    private static final long OPENING_SOON_DAYS = 3;
    private static final long CLOSING_SOON_DAYS = 3;
    private static final long CLOSED_RECENTLY_DAYS = 2;

    private final LoadSurveyPort loadSurveyPort;
    private final LoadSurveyRecipientPort loadSurveyRecipientPort;

    @Override
    public List<StudentNotificationResult> getNotifications(Integer studentId) {
        LocalDateTime now = LocalDateTime.now();
        List<StudentNotificationResult> notifications = new ArrayList<>();

        for (SurveyRecipient recipient : loadSurveyRecipientPort.loadByStudentId(studentId)) {
            Survey survey = loadSurveyPort.loadById(recipient.getSurveyId()).orElse(null);
            if (survey == null) {
                continue;
            }
            notifications.addAll(toNotifications(survey, recipient, now));
        }

        return notifications.stream()
                .sorted(Comparator.comparing(StudentNotificationResult::eventAt).reversed())
                .toList();
    }

    private List<StudentNotificationResult> toNotifications(Survey survey, SurveyRecipient recipient, LocalDateTime now) {
        List<StudentNotificationResult> notifications = new ArrayList<>();
        SurveyStatus status = survey.statusAt(now);

        if (!survey.isPublished() || survey.isHidden()) {
            return notifications;
        }

        if (status == SurveyStatus.OPEN && !recipient.hasSubmitted() && isWithinPastDays(survey.getStartDate(), now, NEW_SURVEY_DAYS)) {
            notifications.add(new StudentNotificationResult(
                    "NEW_SURVEY",
                    "New survey available",
                    "A new survey is open for responses: " + survey.getTitle(),
                    survey.getId(),
                    survey.getTitle(),
                    "Open survey",
                    safeEventAt(survey.getStartDate(), now)
            ));
        }

        if (status == SurveyStatus.NOT_OPEN && isWithinNextDays(survey.getStartDate(), now, OPENING_SOON_DAYS)) {
            notifications.add(new StudentNotificationResult(
                    "OPENING_SOON",
                    "Survey opening soon",
                    "The survey \"" + survey.getTitle() + "\" will open soon.",
                    survey.getId(),
                    survey.getTitle(),
                    "View surveys",
                    safeEventAt(survey.getStartDate(), now)
            ));
        }

        if (status == SurveyStatus.OPEN && !recipient.hasSubmitted() && isWithinNextDays(survey.getEndDate(), now, CLOSING_SOON_DAYS)) {
            notifications.add(new StudentNotificationResult(
                    "CLOSING_SOON",
                    "Survey closing soon",
                    "The survey \"" + survey.getTitle() + "\" is closing soon. Submit before the deadline.",
                    survey.getId(),
                    survey.getTitle(),
                    "Respond now",
                    safeEventAt(survey.getEndDate(), now)
            ));
        }

        if (status == SurveyStatus.CLOSED && !recipient.hasSubmitted() && isWithinPastDays(survey.getEndDate(), now, CLOSED_RECENTLY_DAYS)) {
            notifications.add(new StudentNotificationResult(
                    "CLOSED",
                    "Survey closed",
                    "The survey \"" + survey.getTitle() + "\" has closed.",
                    survey.getId(),
                    survey.getTitle(),
                    "View surveys",
                    safeEventAt(survey.getEndDate(), now)
            ));
        }

        return notifications;
    }

    private boolean isWithinPastDays(LocalDateTime value, LocalDateTime now, long days) {
        if (value == null || value.isAfter(now)) {
            return false;
        }

        return Duration.between(value, now).toDays() <= days;
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
