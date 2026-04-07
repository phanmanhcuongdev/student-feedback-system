package com.ttcs.backend.application.port.in.auth.command;

public record RegisterStudentCommand(
        String name,
        String email,
        String password,
        String studentCode,
        String departmentName
) {
}
