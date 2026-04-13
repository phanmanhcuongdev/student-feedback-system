package com.ttcs.backend.adapter.in.web.dto;

public record ManagedUserSummaryResponse(
        Integer id,
        String email,
        String role,
        String name,
        String departmentName,
        String studentStatus,
        boolean active
) {
}
