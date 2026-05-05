package com.ttcs.backend.infrastructure.scheduling;

import com.ttcs.backend.application.port.in.resultview.SendNotificationToUserUseCase;
import com.ttcs.backend.application.port.out.LoadStudentNotificationPort;
import com.ttcs.backend.application.port.out.LoadSurveyReminderCandidatePort;
import com.ttcs.backend.application.port.out.SurveyReminderCandidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SurveyReminderTask {

    private static final String REMINDER_TYPE = "SURVEY_DEADLINE_REMINDER";

    private final LoadSurveyReminderCandidatePort loadSurveyReminderCandidatePort;
    private final LoadStudentNotificationPort loadStudentNotificationPort;
    private final SendNotificationToUserUseCase sendNotificationToUserUseCase;

    @Value("${app.notifications.survey-reminder.window-hours:24}")
    private long reminderWindowHours;

    @Scheduled(
            cron = "${app.notifications.survey-reminder.cron:0 */5 * * * *}",
            zone = "${app.notifications.survey-reminder.zone:Asia/Ho_Chi_Minh}"
    )
    public void sendSurveyDeadlineReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reminderDate = now.toLocalDate();
        LocalDateTime deadlineTo = now.plusHours(Math.max(reminderWindowHours, 1));

        for (SurveyReminderCandidate candidate : loadSurveyReminderCandidatePort.loadUnsubmittedSurveysClosingBetween(now, deadlineTo)) {
            try {
                sendReminderIfNeeded(candidate, reminderDate);
            } catch (Exception ex) {
                log.warn(
                        "Failed to send survey reminder. userId={}, surveyId={}",
                        candidate.userId(),
                        candidate.surveyId(),
                        ex
                );
            }
        }
    }

    private void sendReminderIfNeeded(SurveyReminderCandidate candidate, LocalDate reminderDate) {
        if (alreadySentToday(candidate, reminderDate)) {
            return;
        }
        sendNotificationToUserUseCase.sendSurveyNotificationToUser(
                candidate.userId(),
                "The survey \"" + candidate.surveyTitle() + "\" is closing soon. Submit before the deadline.",
                REMINDER_TYPE,
                candidate.surveyId(),
                "Survey closing soon",
                "Respond now",
                metadata(candidate, reminderDate)
        );
    }

    private boolean alreadySentToday(SurveyReminderCandidate candidate, LocalDate reminderDate) {
        return loadStudentNotificationPort.existsForUserAndSurveyOnDate(
                candidate.userId(),
                REMINDER_TYPE,
                candidate.surveyId(),
                reminderDate
        );
    }

    private String metadata(SurveyReminderCandidate candidate, LocalDate reminderDate) {
        return "reminderDate=" + reminderDate
                + ";deadline=" + candidate.deadline();
    }
}
