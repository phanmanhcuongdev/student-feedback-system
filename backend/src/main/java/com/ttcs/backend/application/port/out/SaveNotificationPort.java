package com.ttcs.backend.application.port.out;

public interface SaveNotificationPort {
    void create(NotificationCreateCommand command);
}
