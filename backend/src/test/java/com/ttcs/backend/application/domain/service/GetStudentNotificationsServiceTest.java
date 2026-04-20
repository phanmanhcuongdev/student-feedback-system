package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsQuery;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.application.port.out.LoadStudentNotificationPort;
import com.ttcs.backend.application.port.out.LoadedStudentNotification;
import com.ttcs.backend.application.port.out.LoadedStudentNotificationPage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetStudentNotificationsServiceTest {

    @Test
    void shouldLoadNotificationsWithoutCreatingDeadlineReminders() {
        InMemoryNotificationPort notificationPort = new InMemoryNotificationPort();
        GetStudentNotificationsService service = new GetStudentNotificationsService(notificationPort);

        List<StudentNotificationResult> results = service.getNotifications(new GetStudentNotificationsQuery(0, 10), 10).items();

        assertTrue(results.isEmpty());
        assertEquals(1, notificationPort.loadPageCalls);
        assertEquals(0, notificationPort.markAsReadCalls);
        assertEquals(0, notificationPort.markAllAsReadCalls);
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
        GetStudentNotificationsService service = new GetStudentNotificationsService(notificationPort);

        List<StudentNotificationResult> results = service.getNotifications(new GetStudentNotificationsQuery(0, 10), 10).items();

        assertEquals(1, results.size());
        assertEquals("SURVEY_PUBLISHED", results.getFirst().type());
        assertFalse(results.getFirst().read());
    }

    private static final class InMemoryNotificationPort implements LoadStudentNotificationPort {
        private final List<LoadedStudentNotification> notifications = new java.util.ArrayList<>();
        private int loadPageCalls;
        private int markAsReadCalls;
        private int markAllAsReadCalls;

        @Override
        public LoadedStudentNotificationPage loadPage(Integer userId, int page, int size, boolean unreadOnly) {
            loadPageCalls++;
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
            markAsReadCalls++;
            return true;
        }

        @Override
        public int markAllAsRead(Integer userId, LocalDateTime readAt) {
            markAllAsReadCalls++;
            return 0;
        }
    }
}
