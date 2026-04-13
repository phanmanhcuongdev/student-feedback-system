package com.ttcs.backend.application.port.in.admin;

public record ManagedUserDetailResult(
        Integer id,
        String email,
        String role,
        boolean active,
        String name,
        Integer departmentId,
        String departmentName,
        String studentCode,
        String teacherCode,
        String studentStatus
) {
}
