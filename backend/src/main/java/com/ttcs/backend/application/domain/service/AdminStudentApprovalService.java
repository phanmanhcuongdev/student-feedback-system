package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.AuditActionType;
import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.domain.model.AuditTargetType;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.port.in.admin.ApprovalActionResult;
import com.ttcs.backend.application.port.in.admin.ApproveStudentUseCase;
import com.ttcs.backend.application.port.in.admin.GetPendingStudentsQuery;
import com.ttcs.backend.application.port.in.admin.GetStudentDocumentUseCase;
import com.ttcs.backend.application.port.in.admin.GetPendingStudentsUseCase;
import com.ttcs.backend.application.port.in.admin.PendingStudentPageResult;
import com.ttcs.backend.application.port.in.admin.PendingStudentResult;
import com.ttcs.backend.application.port.in.admin.RejectStudentUseCase;
import com.ttcs.backend.application.port.in.admin.StudentDocumentResult;
import com.ttcs.backend.application.port.out.admin.LoadPendingStudentsPort;
import com.ttcs.backend.application.port.out.admin.ManagePendingStudentsQuery;
import com.ttcs.backend.application.port.out.admin.PendingStudentSearchItem;
import com.ttcs.backend.application.port.out.SaveAuditLogPort;
import com.ttcs.backend.application.port.out.NotificationCreateCommand;
import com.ttcs.backend.application.port.out.SaveNotificationPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import com.ttcs.backend.application.port.out.auth.SaveStudentPort;
import com.ttcs.backend.application.port.out.auth.StoreStudentDocumentPort;
import com.ttcs.backend.application.port.out.auth.StudentDocumentContent;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@UseCase
@RequiredArgsConstructor
@Transactional
public class AdminStudentApprovalService implements
        GetPendingStudentsUseCase,
        ApproveStudentUseCase,
        RejectStudentUseCase,
        GetStudentDocumentUseCase {

    private final LoadPendingStudentsPort loadPendingStudentsPort;
    private final LoadStudentByIdPort loadStudentByIdPort;
    private final SaveStudentPort saveStudentPort;
    private final SaveAuditLogPort saveAuditLogPort;
    private final SaveNotificationPort saveNotificationPort;
    private final StoreStudentDocumentPort storeStudentDocumentPort;

    @Override
    @Transactional(readOnly = true)
    public PendingStudentPageResult getPendingStudents(GetPendingStudentsQuery query) {
        var page = loadPendingStudentsPort.loadPage(new ManagePendingStudentsQuery(
                query == null ? null : query.keyword(),
                query == null ? null : query.departmentId(),
                query == null ? null : query.submissionType(),
                query == null ? 0 : query.page(),
                query == null ? 10 : query.size(),
                query == null ? "resubmissionCount" : query.sortBy(),
                query == null ? "desc" : query.sortDir()
        ));

        return new PendingStudentPageResult(
                page.items().stream().map(this::toPendingStudentResult).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
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
        if (studentId == null || reviewerUserId == null) {
            return ApprovalActionResult.fail("INVALID_INPUT", "Student id and reviewer id are required");
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
        saveAuditLogPort.save(new AuditLog(
                null,
                reviewerUserId,
                targetStatus == Status.ACTIVE ? AuditActionType.ONBOARDING_APPROVED : AuditActionType.ONBOARDING_REJECTED,
                AuditTargetType.STUDENT,
                student.getId(),
                targetStatus == Status.ACTIVE ? "Approved onboarding review" : "Rejected onboarding review",
                buildDecisionDetails(student, reviewReason, reviewNotes),
                student.getStatus().name(),
                targetStatus.name(),
                null
        ));
        saveNotificationPort.create(new NotificationCreateCommand(
                targetStatus == Status.ACTIVE ? "ONBOARDING_APPROVED" : "ONBOARDING_REJECTED",
                targetStatus == Status.ACTIVE ? "Onboarding approved" : "Onboarding needs revision",
                targetStatus == Status.ACTIVE
                        ? "Your student account has been approved."
                        : "Your onboarding submission was rejected. Review the reason and resubmit your documents.",
                null,
                targetStatus == Status.ACTIVE ? "View dashboard" : "Review status",
                reviewerUserId,
                buildDecisionDetails(student, reviewReason, reviewNotes),
                java.util.List.of(student.getId())
        ));

        return ApprovalActionResult.success(code, message);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDocumentResult getDocument(Integer studentId, String documentType) {
        if (studentId == null || isBlank(documentType)) {
            throw new IllegalArgumentException("INVALID_REQUEST");
        }

        Student student = loadStudentByIdPort.loadById(studentId).orElse(null);
        if (student == null) {
            throw new IllegalArgumentException("STUDENT_NOT_FOUND");
        }

        String documentPath = switch (documentType.trim().toLowerCase()) {
            case "student-card" -> student.getStudentCardImageUrl();
            case "national-id" -> student.getNationalIdImageUrl();
            default -> throw new IllegalArgumentException("INVALID_DOCUMENT_TYPE");
        };

        if (isBlank(documentPath)) {
            throw new IllegalArgumentException("DOCUMENT_NOT_FOUND");
        }

        StudentDocumentContent content = storeStudentDocumentPort.load(documentPath);
        return new StudentDocumentResult(content.filename(), content.contentType(), content.content());
    }

    private String buildDecisionDetails(Student student, String reviewReason, String reviewNotes) {
        StringBuilder details = new StringBuilder();
        details.append("studentCode=").append(student.getStudentCode());
        if (student.getDepartment() != null) {
            details.append("; department=").append(student.getDepartment().getName());
        }
        if (!isBlank(reviewReason)) {
            details.append("; reason=").append(reviewReason.trim());
        }
        if (!isBlank(reviewNotes)) {
            details.append("; notes=").append(reviewNotes.trim());
        }
        return details.toString();
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

    private PendingStudentResult toPendingStudentResult(PendingStudentSearchItem item) {
        return new PendingStudentResult(
                item.id(),
                item.name(),
                item.email(),
                item.studentCode(),
                item.departmentName(),
                item.status(),
                item.studentCardImageUrl(),
                item.nationalIdImageUrl(),
                item.reviewReason(),
                item.reviewNotes(),
                item.resubmissionCount()
        );
    }
}
