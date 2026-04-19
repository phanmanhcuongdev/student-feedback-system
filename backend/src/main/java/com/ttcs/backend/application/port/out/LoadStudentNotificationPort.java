package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public interface LoadStudentNotificationPort {
    LoadedStudentNotificationPage loadPage(Integer userId, int page, int size, boolean unreadOnly);

    boolean existsForUserAndSurvey(Integer userId, String type, Integer surveyId);

    boolean markAsRead(Integer notificationUserId, Integer userId, LocalDateTime readAt);

    int markAllAsRead(Integer userId, LocalDateTime readAt);
}
