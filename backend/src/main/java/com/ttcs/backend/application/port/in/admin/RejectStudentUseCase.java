package com.ttcs.backend.application.port.in.admin;

public interface RejectStudentUseCase {
    ApprovalActionResult reject(Integer studentId, String reviewReason, String reviewNotes, Integer reviewerUserId);
}
