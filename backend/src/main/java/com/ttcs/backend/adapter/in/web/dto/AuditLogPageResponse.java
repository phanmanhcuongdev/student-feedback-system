package com.ttcs.backend.adapter.in.web.dto;

import java.util.List;

public record AuditLogPageResponse(
        List<AuditLogResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
