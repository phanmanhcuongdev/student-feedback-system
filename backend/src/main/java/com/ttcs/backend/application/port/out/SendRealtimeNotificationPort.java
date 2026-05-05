package com.ttcs.backend.application.port.out;

public interface SendRealtimeNotificationPort {
    void sendToUser(Integer userId, RealtimeNotificationMessage message);
}
