package com.ttcs.backend.application.port.in.admin;

public record PendingStudentResult(
        Integer id,
        String name,
        String email,
        String studentCode,
        String departmentName,
        String status,
        String studentCardImageUrl,
        String nationalIdImageUrl
) {
}
