package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsQuery;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.application.port.out.LoadStudentNotificationPort;
import com.ttcs.backend.application.port.out.LoadStudentSurveysQuery;
import com.ttcs.backend.application.port.out.LoadSurveyPort;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.LoadedStudentNotification;
import com.ttcs.backend.application.port.out.LoadedStudentNotificationPage;
import com.ttcs.backend.application.port.out.NotificationCreateCommand;
import com.ttcs.backend.application.port.out.SaveNotificationPort;
import com.ttcs.backend.application.port.out.StudentSurveySearchPage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetStudentNotificationsServiceTest {

    @Test
    void shouldCreatePersistedDeadlineReminderForOpenUnsubmittedSurvey() {
        Survey survey = new Survey(
                1,
                "Midterm Feedback",
                "Provide feedback",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusHours(12),
                1,
                false,
                SurveyLifecycleState.PUBLISHED
        );
        InMemoryNotificationPort notificationPort = new InMemoryNotificationPort();
        GetStudentNotificationsService service = new GetStudentNotificationsService(
                surveyPort(List.of(survey)),
                recipientPort(new SurveyRecipient(1, 1, 10, LocalDateTime.now().minusDays(1), null, null)),
                notificationPort,
                notificationPort
        );

        List<StudentNotificationResult> results = service.getNotifications(new GetStudentNotificationsQuery(0, 10), 10).items();

        assertEquals(1, results.size());
        assertTrue(results.stream().anyMatch(item -> item.type().equals("SURVEY_DEADLINE_REMINDER")));
        assertEquals(1, notificationPort.createdCommands.size());
    }

    @Test
    void shouldSkipDeadlineReminderForSubmittedSurvey() {
        Survey survey = new Survey(
                1,
                "Midterm Feedback",
                "Provide feedback",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusHours(12),
                1,
                false,
                SurveyLifecycleState.PUBLISHED
        );
        InMemoryNotificationPort notificationPort = new InMemoryNotificationPort();
        GetStudentNotificationsService service = new GetStudentNotificationsService(
                surveyPort(List.of(survey)),
                recipientPort(new SurveyRecipient(1, 1, 10, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(6), LocalDateTime.now().minusHours(1))),
                notificationPort,
                notificationPort
        );

        List<StudentNotificationResult> results = service.getNotifications(new GetStudentNotificationsQuery(0, 10), 10).items();

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnExistingPersistedNotification() {
        InMemoryNotificationPort notificationPort = new InMemoryNotificationPort();
        notificationPort.notifications.add(new LoadedStudentNotification(
                7,
                "SURVEY_PUBLISHED",
                "New survey available",
                "A new survey is available.",
                2,
                "Final Feedback",
                "Open survey",
                LocalDateTime.now(),
                null
        ));
        GetStudentNotificationsService service = new GetStudentNotificationsService(
                surveyPort(List.of()),
                recipientPort(null),
                notificationPort,
                notificationPort
        );

        List<StudentNotificationResult> results = service.getNotifications(new GetStudentNotificationsQuery(0, 10), 10).items();

        assertEquals(1, results.size());
        assertEquals("SURVEY_PUBLISHED", results.getFirst().type());
        assertFalse(results.getFirst().read());
    }

    private LoadSurveyPort surveyPort(List<Survey> surveys) {
        return new LoadSurveyPort() {
            @Override
            public Optional<Survey> loadById(Integer surveyId) {
                return surveys.stream().filter(survey -> survey.getId().equals(surveyId)).findFirst();
            }

            @Override
            public List<Survey> loadAll() {
                return surveys;
            }

            @Override
            public StudentSurveySearchPage loadStudentSurveyPage(LoadStudentSurveysQuery query) {
                return new StudentSurveySearchPage(List.of(), 0, 0, 0, 0);
            }
        };
    }

    private LoadSurveyRecipientPort recipientPort(SurveyRecipient recipient) {
        return new LoadSurveyRecipientPort() {
            @Override
            public Optional<SurveyRecipient> loadBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
                return Optional.ofNullable(recipient);
            }

            @Override
            public List<SurveyRecipient> loadBySurveyId(Integer surveyId) {
                return recipient == null ? List.of() : List.of(recipient);
            }

            @Override
            public List<SurveyRecipient> loadByStudentId(Integer studentId) {
                return recipient == null ? List.of() : List.of(recipient);
            }
        };
    }

    private static final class InMemoryNotificationPort implements LoadStudentNotificationPort, SaveNotificationPort {
        private final List<LoadedStudentNotification> notifications = new java.util.ArrayList<>();
        private final List<NotificationCreateCommand> createdCommands = new java.util.ArrayList<>();

        @Override
        public LoadedStudentNotificationPage loadPage(Integer userId, int page, int size, boolean unreadOnly) {
            return new LoadedStudentNotificationPage(
                    notifications.stream()
                            .filter(item -> !unreadOnly || item.readAt() == null)
                            .toList(),
                    page,
                    size,
                    notifications.size(),
                    notifications.isEmpty() ? 0 : 1,
                    notifications.stream().filter(item -> item.readAt() == null).count()
            );
        }

        @Override
        public boolean existsForUserAndSurvey(Integer userId, String type, Integer surveyId) {
            return notifications.stream().anyMatch(item ->
                    item.type().equals(type) && item.surveyId() != null && item.surveyId().equals(surveyId)
            );
        }

        @Override
        public boolean markAsRead(Integer notificationUserId, Integer userId, LocalDateTime readAt) {
            return true;
        }

        @Override
        public int markAllAsRead(Integer userId, LocalDateTime readAt) {
            return 0;
        }

        @Override
        public void create(NotificationCreateCommand command) {
            createdCommands.add(command);
            notifications.add(new LoadedStudentNotification(
                    createdCommands.size(),
                    command.type(),
                    command.title(),
                    command.content(),
                    command.surveyId(),
                    command.surveyId() == null ? null : "Survey " + command.surveyId(),
                    command.actionLabel(),
                    LocalDateTime.now(),
                    null
            ));
        }
    }
}
