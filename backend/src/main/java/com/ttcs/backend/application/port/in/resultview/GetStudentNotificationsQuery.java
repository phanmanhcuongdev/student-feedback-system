package com.ttcs.backend.application.port.in.resultview;

public record GetStudentNotificationsQuery(
        int page,
        int size
) {
}
