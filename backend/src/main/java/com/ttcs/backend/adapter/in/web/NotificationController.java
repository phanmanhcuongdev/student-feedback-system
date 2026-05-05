package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.NotificationActionResponse;
import com.ttcs.backend.adapter.in.web.dto.StudentNotificationResponse;
import com.ttcs.backend.adapter.in.web.dto.StudentNotificationPageResponse;
import com.ttcs.backend.adapter.in.web.dto.UnreadNotificationCountResponse;
import com.ttcs.backend.application.port.in.resultview.CountUnreadNotificationsUseCase;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsQuery;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsUseCase;
import com.ttcs.backend.application.port.in.resultview.MarkStudentNotificationReadUseCase;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationPageResult;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@WebAdapter
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetStudentNotificationsUseCase getStudentNotificationsUseCase;
    private final MarkStudentNotificationReadUseCase markStudentNotificationReadUseCase;
    private final CountUnreadNotificationsUseCase countUnreadNotificationsUseCase;
    private final CurrentIdentityProvider currentIdentityProvider;

    @GetMapping
    public ResponseEntity<StudentNotificationPageResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        Integer studentUserId = currentStudentUserId();
        StudentNotificationPageResult result = getStudentNotificationsUseCase.getNotifications(
                new GetStudentNotificationsQuery(page, size, unreadOnly),
                studentUserId
        );
        return ResponseEntity.ok(new StudentNotificationPageResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.unreadCount()
        ));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> getUnreadCount() {
        return ResponseEntity.ok(new UnreadNotificationCountResponse(
                countUnreadNotificationsUseCase.countUnreadNotifications(currentStudentUserId())
        ));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationActionResponse> markAsRead(@PathVariable Integer notificationId) {
        boolean success = markStudentNotificationReadUseCase.markAsRead(notificationId, currentStudentUserId());
        return ResponseEntity.ok(new NotificationActionResponse(
                success,
                success ? "NOTIFICATION_MARKED_READ" : "NOTIFICATION_NOT_FOUND",
                success ? "Notification marked as read." : "Notification was not found."
        ));
    }

    @PostMapping("/read-all")
    public ResponseEntity<NotificationActionResponse> markAllAsRead() {
        int count = markStudentNotificationReadUseCase.markAllAsRead(currentStudentUserId());
        return ResponseEntity.ok(new NotificationActionResponse(
                true,
                "NOTIFICATIONS_MARKED_READ",
                count + " notification" + (count == 1 ? "" : "s") + " marked as read."
        ));
    }

    private StudentNotificationResponse toResponse(StudentNotificationResult result) {
        return new StudentNotificationResponse(
                result.id(),
                result.type(),
                result.title(),
                result.message(),
                result.surveyId(),
                result.surveyTitle(),
                result.actionLabel(),
                result.eventAt(),
                result.read(),
                result.readAt()
        );
    }

    private Integer currentStudentUserId() {
        if (currentIdentityProvider.currentRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can use the notification center");
        }
        return currentIdentityProvider.currentUserId();
    }
}
