package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.AuditActionType;
import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.admin.ApprovalActionResult;
import com.ttcs.backend.application.port.out.admin.LoadPendingStudentsPort;
import com.ttcs.backend.application.port.out.admin.PendingStudentSearchItem;
import com.ttcs.backend.application.port.out.admin.PendingStudentSearchPage;
import com.ttcs.backend.application.port.out.SaveAuditLogPort;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import com.ttcs.backend.application.port.out.auth.SaveStudentPort;
import com.ttcs.backend.application.port.out.auth.StoreStudentDocumentPort;
import com.ttcs.backend.application.port.out.auth.StudentDocumentContent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminStudentApprovalServiceTest {

    @Test
    void shouldRejectPendingStudentWithReasonAndNotes() {
        Student pendingStudent = pendingStudent();
        RecordingSaveStudentPort saveStudentPort = new RecordingSaveStudentPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminStudentApprovalService service = new AdminStudentApprovalService(
                pendingPort(List.of(pendingStudent)),
                loadStudentPort(pendingStudent),
                saveStudentPort,
                auditLogPort,
                command -> {
                },
                noOpDocumentPort()
        );

        ApprovalActionResult result = service.reject(5, "Photo unreadable", "Student card edges are cut off.", 1);

        assertTrue(result.success());
        assertEquals("REJECT_SUCCESS", result.code());
        assertNotNull(saveStudentPort.lastSavedStudent);
        assertEquals(Status.REJECTED, saveStudentPort.lastSavedStudent.getStatus());
        assertEquals("Photo unreadable", saveStudentPort.lastSavedStudent.getReviewReason());
        assertEquals("Student card edges are cut off.", saveStudentPort.lastSavedStudent.getReviewNotes());
        assertEquals(1, saveStudentPort.lastSavedStudent.getReviewedByUserId());
        assertNotNull(saveStudentPort.lastSavedStudent.getReviewedAt());
        assertEquals(1, auditLogPort.savedLogs.size());
        assertEquals(AuditActionType.ONBOARDING_REJECTED, auditLogPort.savedLogs.getFirst().getActionType());
        assertEquals("PENDING", auditLogPort.savedLogs.getFirst().getOldState());
        assertEquals("REJECTED", auditLogPort.savedLogs.getFirst().getNewState());
        assertTrue(auditLogPort.savedLogs.getFirst().getDetails().contains("reason=Photo unreadable"));
    }

    @Test
    void shouldRequireReasonWhenRejectingStudent() {
        Student pendingStudent = pendingStudent();
        RecordingSaveStudentPort saveStudentPort = new RecordingSaveStudentPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminStudentApprovalService service = new AdminStudentApprovalService(
                pendingPort(List.of(pendingStudent)),
                loadStudentPort(pendingStudent),
                saveStudentPort,
                auditLogPort,
                command -> {
                },
                noOpDocumentPort()
        );

        ApprovalActionResult result = service.reject(5, "   ", "Missing reason", 1);

        assertFalse(result.success());
        assertEquals("REVIEW_REASON_REQUIRED", result.code());
        assertNull(saveStudentPort.lastSavedStudent);
        assertTrue(auditLogPort.savedLogs.isEmpty());
    }

    @Test
    void shouldApprovePendingStudentAndPersistReviewerNotes() {
        Student pendingStudent = pendingStudent();
        RecordingSaveStudentPort saveStudentPort = new RecordingSaveStudentPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminStudentApprovalService service = new AdminStudentApprovalService(
                pendingPort(List.of(pendingStudent)),
                loadStudentPort(pendingStudent),
                saveStudentPort,
                auditLogPort,
                command -> {
                },
                noOpDocumentPort()
        );

        ApprovalActionResult result = service.approve(5, "Identity documents verified.", 2);

        assertTrue(result.success());
        assertEquals("APPROVE_SUCCESS", result.code());
        assertEquals(Status.ACTIVE, saveStudentPort.lastSavedStudent.getStatus());
        assertNull(saveStudentPort.lastSavedStudent.getReviewReason());
        assertEquals("Identity documents verified.", saveStudentPort.lastSavedStudent.getReviewNotes());
        assertEquals(2, saveStudentPort.lastSavedStudent.getReviewedByUserId());
        assertEquals(1, auditLogPort.savedLogs.size());
        assertEquals(AuditActionType.ONBOARDING_APPROVED, auditLogPort.savedLogs.getFirst().getActionType());
        assertEquals("PENDING", auditLogPort.savedLogs.getFirst().getOldState());
        assertEquals("ACTIVE", auditLogPort.savedLogs.getFirst().getNewState());
    }

    @Test
    void shouldNotCreateAuditLogWhenReviewerIdIsMissing() {
        Student pendingStudent = pendingStudent();
        RecordingSaveStudentPort saveStudentPort = new RecordingSaveStudentPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminStudentApprovalService service = new AdminStudentApprovalService(
                pendingPort(List.of(pendingStudent)),
                loadStudentPort(pendingStudent),
                saveStudentPort,
                auditLogPort,
                command -> {
                },
                noOpDocumentPort()
        );

        ApprovalActionResult result = service.approve(5, "Identity documents verified.", null);

        assertFalse(result.success());
        assertEquals("INVALID_INPUT", result.code());
        assertNull(saveStudentPort.lastSavedStudent);
        assertTrue(auditLogPort.savedLogs.isEmpty());
    }

    private LoadPendingStudentsPort pendingPort(List<Student> students) {
        return query -> new PendingStudentSearchPage(
                students.stream()
                        .map(student -> new PendingStudentSearchItem(
                                student.getId(),
                                student.getName(),
                                student.getUser().getEmail(),
                                student.getStudentCode(),
                                student.getDepartment() == null ? null : student.getDepartment().getName(),
                                student.getStatus().name(),
                                student.getStudentCardImageUrl(),
                                student.getNationalIdImageUrl(),
                                student.getReviewReason(),
                                student.getReviewNotes(),
                                student.getResubmissionCount()
                        ))
                        .toList(),
                0,
                students.size(),
                students.size(),
                students.isEmpty() ? 0 : 1
        );
    }

    private LoadStudentByIdPort loadStudentPort(Student student) {
        return new LoadStudentByIdPort() {
            @Override
            public Optional<Student> loadById(Integer studentId) {
                return Optional.ofNullable(student);
            }

            @Override
            public Optional<Student> loadByUserId(Integer userId) {
                return Optional.ofNullable(student);
            }
        };
    }

    private StoreStudentDocumentPort noOpDocumentPort() {
        return new StoreStudentDocumentPort() {
            @Override
            public String save(org.springframework.web.multipart.MultipartFile file, String prefix) {
                return prefix + "-path";
            }

            @Override
            public StudentDocumentContent load(String location) {
                return new StudentDocumentContent("document.png", "image/png", new byte[]{1});
            }
        };
    }

    private Student pendingStudent() {
        return new Student(
                5,
                new User(5, "student.pending@university.edu", "secret", Role.STUDENT, true),
                "Pending Approval Student",
                "S0003",
                new Department(2, "Information Systems"),
                Status.PENDING,
                "/docs/student-card-s0003.png",
                "/docs/national-id-s0003.png",
                null,
                null,
                null,
                null,
                0
        );
    }

    private static final class RecordingSaveStudentPort implements SaveStudentPort {
        private Student lastSavedStudent;

        @Override
        public Student save(Student student) {
            lastSavedStudent = student;
            return student;
        }

        @Override
        public boolean existsByStudentCode(String studentCode) {
            return false;
        }
    }

    private static final class RecordingAuditLogPort implements SaveAuditLogPort {
        private final List<AuditLog> savedLogs = new ArrayList<>();

        @Override
        public AuditLog save(AuditLog auditLog) {
            savedLogs.add(auditLog);
            return auditLog;
        }
    }
}
