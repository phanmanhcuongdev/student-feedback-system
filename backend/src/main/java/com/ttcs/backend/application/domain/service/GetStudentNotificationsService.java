package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsQuery;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsUseCase;
import com.ttcs.backend.application.port.in.resultview.MarkStudentNotificationReadUseCase;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationPageResult;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.application.port.out.LoadStudentNotificationPort;
import com.ttcs.backend.application.port.out.LoadedStudentNotificationPage;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@UseCase
@RequiredArgsConstructor
public class GetStudentNotificationsService implements GetStudentNotificationsUseCase, MarkStudentNotificationReadUseCase {

    private final LoadStudentNotificationPort loadStudentNotificationPort;

    @Override
    public StudentNotificationPageResult getNotifications(GetStudentNotificationsQuery query, Integer studentUserId) {
        int page = Math.max(query == null ? 0 : query.page(), 0);
        int size = Math.min(Math.max(query == null ? 6 : query.size(), 1), 100);
        boolean unreadOnly = query != null && query.unreadOnly();
        LoadedStudentNotificationPage result = loadStudentNotificationPort.loadPage(studentUserId, page, size, unreadOnly);

        return new StudentNotificationPageResult(
                result.items().stream()
                        .map(item -> new StudentNotificationResult(
                                item.id(),
                                item.type(),
                                item.title(),
                                item.message(),
                                item.surveyId(),
                                item.surveyTitle(),
                                item.actionLabel(),
                                item.eventAt(),
                                item.readAt() != null,
                                item.readAt()
                        ))
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.unreadCount()
        );
    }

    @Override
    public boolean markAsRead(Integer notificationId, Integer userId) {
        if (notificationId == null || userId == null) {
            return false;
        }
        return loadStudentNotificationPort.markAsRead(notificationId, userId, LocalDateTime.now());
    }

    @Override
    public int markAllAsRead(Integer userId) {
        if (userId == null) {
            return 0;
        }
        return loadStudentNotificationPort.markAllAsRead(userId, LocalDateTime.now());
    }
}
