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
                        student.getNationalIdImageUrl()
                ))
                .toList();
    }

    @Override
    public ApprovalActionResult approve(Integer studentId) {
        return updateStatus(studentId, Status.ACTIVE, "APPROVE_SUCCESS", "Student approved successfully");
    }

    @Override
    public ApprovalActionResult reject(Integer studentId) {
        return updateStatus(studentId, Status.REJECTED, "REJECT_SUCCESS", "Student rejected successfully");
    }

    private ApprovalActionResult updateStatus(Integer studentId, Status targetStatus, String code, String message) {
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
                student.getNationalIdImageUrl()
        );
        saveStudentPort.save(updatedStudent);

        return ApprovalActionResult.success(code, message);
    }
}
