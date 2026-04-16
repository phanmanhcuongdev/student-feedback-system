package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuditLog {
    private final Integer id;
    private final Integer actorUserId;
    private final AuditActionType actionType;
    private final AuditTargetType targetType;
    private final Integer targetId;
    private final String summary;
    private final String details;
    private final String oldState;
    private final String newState;
    private final LocalDateTime createdAt;
}
