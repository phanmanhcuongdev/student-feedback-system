package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public interface LoadStudentNotificationPort {
    LoadedStudentNotificationPage loadPage(Integer studentUserId, int page, int size, boolean unreadOnly);

    boolean existsForUserAndSurvey(Integer studentUserId, String type, Integer surveyId);

    boolean markAsRead(Integer notificationUserId, Integer studentUserId, LocalDateTime readAt);

    int markAllAsRead(Integer studentUserId, LocalDateTime readAt);
}
