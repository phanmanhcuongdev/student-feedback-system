package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDateTime;

public record AuditLogResult(
        Integer id,
        Integer actorUserId,
        String actionType,
        String targetType,
        Integer targetId,
        String summary,
        String details,
        String oldState,
        String newState,
        LocalDateTime createdAt
) {
}
