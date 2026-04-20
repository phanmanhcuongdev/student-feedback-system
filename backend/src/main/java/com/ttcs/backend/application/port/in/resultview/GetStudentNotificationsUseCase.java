package com.ttcs.backend.application.port.in.resultview;

public interface GetStudentNotificationsUseCase {
    StudentNotificationPageResult getNotifications(GetStudentNotificationsQuery query, Integer studentUserId);
}
