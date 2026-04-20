package com.ttcs.backend.application.port.in.admin;

public record UpdateUserCommand(
        Integer userId,
        Integer actorUserId,
        String email,
        String name,
        Integer departmentId,
        String studentCode,
        String lecturerCode
) {
}
