package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.port.in.admin.ApprovalActionResult;
import com.ttcs.backend.application.port.in.admin.ApproveStudentUseCase;
import com.ttcs.backend.application.port.in.admin.GetPendingStudentsUseCase;
import com.ttcs.backend.application.port.in.admin.PendingStudentResult;
import com.ttcs.backend.application.port.in.admin.RejectStudentUseCase;
import com.ttcs.backend.application.port.out.admin.LoadPendingStudentsPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import com.ttcs.backend.application.port.out.auth.SaveStudentPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional
public class AdminStudentApprovalService implements
        GetPendingStudentsUseCase,
        ApproveStudentUseCase,
        RejectStudentUseCase {

    private final LoadPendingStudentsPort loadPendingStudentsPort;
    private final LoadStudentByIdPort loadStudentByIdPort;
    private final SaveStudentPort saveStudentPort;

    @Override
    @Transactional(readOnly = true)
    public List<PendingStudentResult> getPendingStudents() {
        return loadPendingStudentsPort.loadPendingStudents().stream()
                .map(student -> new PendingStudentResult(
                        student.getId(),
                        student.getName(),
                        student.getUser() != null ? student.getUser().getEmail() : null,
                        student.getStudentCode(),
                        student.getDepartment() != null ? student.getDepartment().getName() : null,
                        student.getStatus().name(),
                        student.getStudentCardImageUrl(),
                        student.getNationalIdImageUrl(),
                        student.getReviewReason(),
                        student.getReviewNotes(),
                        student.getResubmissionCount()
                ))
                .toList();
    }

    @Override
    public ApprovalActionResult approve(Integer studentId, String reviewNotes, Integer reviewerUserId) {
        return updateStatus(
                studentId,
                Status.ACTIVE,
                null,
                reviewNotes,
                reviewerUserId,
                "APPROVE_SUCCESS",
                "Student approved successfully"
        );
    }

    @Override
    public ApprovalActionResult reject(Integer studentId, String reviewReason, String reviewNotes, Integer reviewerUserId) {
        if (isBlank(reviewReason)) {
            return ApprovalActionResult.fail("REVIEW_REASON_REQUIRED", "Review reason is required");
        }

        return updateStatus(
                studentId,
                Status.REJECTED,
                reviewReason.trim(),
                reviewNotes,
                reviewerUserId,
                "REJECT_SUCCESS",
                "Student rejected successfully"
        );
    }

    private ApprovalActionResult updateStatus(
            Integer studentId,
            Status targetStatus,
            String reviewReason,
            String reviewNotes,
            Integer reviewerUserId,
            String code,
            String message
    ) {
        if (studentId == null) {
            return ApprovalActionResult.fail("INVALID_INPUT", "Student id is required");
        }

        Student student = loadStudentByIdPort.loadById(studentId).orElse(null);
        if (student == null) {
            return ApprovalActionResult.fail("STUDENT_NOT_FOUND", "Student not found");
        }

        if (student.getStatus() != Status.PENDING) {
            return ApprovalActionResult.fail("INVALID_STATUS", "Student is not pending approval");
        }

        Student updatedStudent = new Student(
                student.getId(),
                student.getUser(),
                student.getName(),
                student.getStudentCode(),
                student.getDepartment(),
                targetStatus,
                student.getStudentCardImageUrl(),
                student.getNationalIdImageUrl(),
                reviewReason,
                normalizeNullableText(reviewNotes),
                reviewerUserId,
                LocalDateTime.now(),
                student.getResubmissionCount() == null ? 0 : student.getResubmissionCount()
        );
        saveStudentPort.save(updatedStudent);

        return ApprovalActionResult.success(code, message);
    }

    private String normalizeNullableText(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
