package com.ttcs.backend.application.port.out.admin;

import java.time.LocalDateTime;

public record AuditLogSearchQuery(
        Integer actorUserId,
        String actionType,
        String targetType,
        Integer targetId,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        String keyword,
        int page,
        int size
) {
}
