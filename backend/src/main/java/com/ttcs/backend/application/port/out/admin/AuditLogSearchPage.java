package com.ttcs.backend.application.port.out.admin;

import com.ttcs.backend.application.domain.model.AuditLog;

import java.util.List;

public record AuditLogSearchPage(
        List<AuditLog> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
