package com.ttcs.backend.application.port.out.admin;

public record PendingStudentSearchItem(
        Integer id,
        String name,
        String email,
        String studentCode,
        String departmentName,
        String status,
        String studentCardImageUrl,
        String nationalIdImageUrl,
        String reviewReason,
        String reviewNotes,
        Integer resubmissionCount
) {
}
