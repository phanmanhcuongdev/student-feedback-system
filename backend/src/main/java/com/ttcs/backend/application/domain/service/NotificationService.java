package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsQuery;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsUseCase;
import com.ttcs.backend.application.port.in.resultview.MarkStudentNotificationReadUseCase;
import com.ttcs.backend.application.port.in.resultview.CountUnreadNotificationsUseCase;
import com.ttcs.backend.application.port.in.resultview.SendNotificationToUserUseCase;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationPageResult;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.application.port.out.LoadStudentNotificationPort;
import com.ttcs.backend.application.port.out.LoadedStudentNotification;
import com.ttcs.backend.application.port.out.LoadedStudentNotificationPage;
import com.ttcs.backend.application.port.out.NotificationCreateCommand;
import com.ttcs.backend.application.port.out.RealtimeNotificationMessage;
import com.ttcs.backend.application.port.out.SaveNotificationPort;
import com.ttcs.backend.application.port.out.SendRealtimeNotificationPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class NotificationService implements
        GetStudentNotificationsUseCase,
        MarkStudentNotificationReadUseCase,
        CountUnreadNotificationsUseCase,
        SendNotificationToUserUseCase {

    private static final String DEFAULT_TITLE = "Notification";

    private final LoadStudentNotificationPort loadStudentNotificationPort;
    private final SaveNotificationPort saveNotificationPort;
    private final SendRealtimeNotificationPort sendRealtimeNotificationPort;

    @Override
    public StudentNotificationPageResult getNotifications(GetStudentNotificationsQuery query, Integer studentUserId) {
        int page = Math.max(query == null ? 0 : query.page(), 0);
        int size = Math.min(Math.max(query == null ? 6 : query.size(), 1), 100);
        boolean unreadOnly = query != null && query.unreadOnly();
        LoadedStudentNotificationPage result = loadStudentNotificationPort.loadPage(studentUserId, page, size, unreadOnly);

        return new StudentNotificationPageResult(
                result.items().stream().map(this::toResult).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.unreadCount()
        );
    }

    public List<StudentNotificationResult> getUnreadNotifications(Integer userId) {
        if (userId == null) {
            return List.of();
        }
        return loadStudentNotificationPort.loadUnread(userId).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public long countUnreadNotifications(Integer userId) {
        if (userId == null) {
            return 0L;
        }
        return loadStudentNotificationPort.countUnread(userId);
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

    @Override
    public void sendNotificationToUser(Integer userId, String message, String type) {
        sendSurveyNotificationToUser(userId, message, type, null, DEFAULT_TITLE, null, null);
    }

    @Override
    public void sendSurveyNotificationToUser(
            Integer userId,
            String message,
            String type,
            Integer surveyId,
            String title,
            String actionLabel,
            String metadata
    ) {
        if (userId == null || isBlank(message) || isBlank(type)) {
            return;
        }

        String resolvedTitle = isBlank(title) ? DEFAULT_TITLE : title;
        List<Integer> notificationRecipientIds = saveNotificationPort.create(new NotificationCreateCommand(
                type,
                resolvedTitle,
                message,
                surveyId,
                actionLabel,
                null,
                metadata,
                List.of(userId)
        ));

        sendRealtimeNotificationPort.sendToUser(userId, new RealtimeNotificationMessage(
                notificationRecipientIds.stream().findFirst().orElse(null),
                type,
                resolvedTitle,
                message,
                surveyId,
                actionLabel,
                LocalDateTime.now()
        ));
    }

    private StudentNotificationResult toResult(LoadedStudentNotification item) {
        return new StudentNotificationResult(
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
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
