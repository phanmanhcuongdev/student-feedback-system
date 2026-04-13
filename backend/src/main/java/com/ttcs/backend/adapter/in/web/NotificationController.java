package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.StudentNotificationResponse;
import com.ttcs.backend.application.port.in.resultview.GetStudentNotificationsUseCase;
import com.ttcs.backend.application.port.in.resultview.StudentNotificationResult;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@WebAdapter
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetStudentNotificationsUseCase getStudentNotificationsUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @GetMapping
    public ResponseEntity<List<StudentNotificationResponse>> getNotifications() {
        currentStudentProvider.ensureActiveStudentAccount();
        Integer studentId = currentStudentProvider.currentStudentId();
        List<StudentNotificationResponse> body = getStudentNotificationsUseCase.getNotifications(studentId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(body);
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
