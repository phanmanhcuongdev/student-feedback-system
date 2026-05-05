package com.ttcs.backend.application.port.out;

import java.util.List;

public interface SaveNotificationPort {
    List<Integer> create(NotificationCreateCommand command);
}
