package com.ttcs.backend.application.port.in.resultview;

import java.util.List;

public interface GetStudentNotificationsUseCase {
    List<StudentNotificationResult> getNotifications(Integer studentId);
}
