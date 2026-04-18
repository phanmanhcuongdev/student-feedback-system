package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.StudentNotificationResponse;
import com.ttcs.backend.adapter.in.web.dto.StudentNotificationPageResponse;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsQuery;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsUseCase;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationPageResult;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@WebAdapter
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetStudentNotificationsUseCase getStudentNotificationsUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @GetMapping
    public ResponseEntity<StudentNotificationPageResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        currentStudentProvider.ensureActiveStudentAccount();
        Integer studentId = currentStudentProvider.currentStudentId();
        StudentNotificationPageResult result = getStudentNotificationsUseCase.getNotifications(
                new GetStudentNotificationsQuery(page, size),
                studentId
        );
        return ResponseEntity.ok(new StudentNotificationPageResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    private StudentNotificationResponse toResponse(StudentNotificationResult result) {
        return new StudentNotificationResponse(
                result.type(),
                result.title(),
                result.message(),
                result.surveyId(),
                result.surveyTitle(),
                result.actionLabel(),
                result.eventAt()
        );
    }
}
