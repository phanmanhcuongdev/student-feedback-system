package com.ttcs.backend.application.port.in.resultview;

public interface CountUnreadNotificationsUseCase {
    long countUnreadNotifications(Integer userId);
}
