package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface LoadStudentNotificationPort {
    LoadedStudentNotificationPage loadPage(Integer studentUserId, int page, int size, boolean unreadOnly);

    List<LoadedStudentNotification> loadUnread(Integer studentUserId);

    long countUnread(Integer studentUserId);

    boolean existsForUserAndSurvey(Integer studentUserId, String type, Integer surveyId);

    boolean existsForUserAndSurveyOnDate(Integer studentUserId, String type, Integer surveyId, java.time.LocalDate date);

    boolean markAsRead(Integer notificationUserId, Integer studentUserId, LocalDateTime readAt);

    int markAllAsRead(Integer studentUserId, LocalDateTime readAt);
}
