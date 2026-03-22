package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationRecipient {
    private final Integer id;
    private final Notification notification;
    private final User user;
}