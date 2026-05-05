package com.ttcs.backend.adapter.out.messaging;

import com.ttcs.backend.application.port.out.RealtimeNotificationMessage;
import com.ttcs.backend.application.port.out.SendRealtimeNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketNotificationAdapter implements SendRealtimeNotificationPort {

    private static final String USER_NOTIFICATION_DESTINATION = "/topic/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendToUser(Integer userId, RealtimeNotificationMessage message) {
        if (userId == null || message == null) {
            return;
        }
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                USER_NOTIFICATION_DESTINATION,
                message
        );
    }
}
