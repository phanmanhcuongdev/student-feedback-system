package com.ttcs.backend.application.port.in.admin;

public record ManagedUserSummaryResult(
        Integer id,
        String email,
        String role,
        String name,
        Integer departmentId,
        String departmentName,
        String studentStatus,
        boolean active,
        String studentCode,
        String lecturerCode
) {
}
