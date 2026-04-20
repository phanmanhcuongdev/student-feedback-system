package com.ttcs.backend.application.port.in.resultview;

public interface MarkStudentNotificationReadUseCase {
    boolean markAsRead(Integer notificationId, Integer userId);

    int markAllAsRead(Integer userId);
}
