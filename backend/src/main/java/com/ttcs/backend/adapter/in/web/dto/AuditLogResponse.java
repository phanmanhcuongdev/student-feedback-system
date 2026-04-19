package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
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
