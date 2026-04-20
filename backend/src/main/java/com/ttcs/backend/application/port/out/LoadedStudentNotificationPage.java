package com.ttcs.backend.application.port.out;

import java.util.List;

public record LoadedStudentNotificationPage(
        List<LoadedStudentNotification> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        long unreadCount
) {
}
