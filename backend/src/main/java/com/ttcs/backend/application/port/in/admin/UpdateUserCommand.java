package com.ttcs.backend.application.port.in.admin;

public record UpdateUserCommand(
        Integer userId,
        String email,
        String name,
        Integer departmentId,
        String studentCode,
        String teacherCode
) {
}
