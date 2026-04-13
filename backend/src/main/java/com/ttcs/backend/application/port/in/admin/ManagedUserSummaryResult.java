package com.ttcs.backend.application.port.in.admin;

public record ManagedUserSummaryResult(
        Integer id,
        String email,
        String role,
        String name,
        String departmentName,
        String studentStatus,
        boolean active
) {
}
