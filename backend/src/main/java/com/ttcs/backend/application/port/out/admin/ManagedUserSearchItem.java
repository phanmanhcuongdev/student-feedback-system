package com.ttcs.backend.application.port.out.admin;

public record ManagedUserSearchItem(
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
