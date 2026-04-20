package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDate;

public record GetAuditLogsQuery(
        Integer actorUserId,
        String actionType,
        String targetType,
        Integer targetId,
        String keyword,
        LocalDate createdFrom,
        LocalDate createdTo,
        int page,
        int size
) {
}
