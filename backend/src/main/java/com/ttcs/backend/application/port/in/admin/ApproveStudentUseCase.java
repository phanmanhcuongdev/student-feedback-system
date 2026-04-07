package com.ttcs.backend.application.port.in.admin;

public interface ApproveStudentUseCase {
    ApprovalActionResult approve(Integer studentId);
}
