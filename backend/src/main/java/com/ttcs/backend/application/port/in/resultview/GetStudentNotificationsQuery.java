package com.ttcs.backend.application.port.in.resultview;

public record GetStudentNotificationsQuery(
        int page,
        int size,
        boolean unreadOnly
) {
    public GetStudentNotificationsQuery(int page, int size) {
        this(page, size, false);
    }
}
