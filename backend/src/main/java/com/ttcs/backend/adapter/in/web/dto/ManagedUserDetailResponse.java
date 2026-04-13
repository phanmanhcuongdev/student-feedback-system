package com.ttcs.backend.adapter.in.web.dto;

public record ManagedUserDetailResponse(
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
