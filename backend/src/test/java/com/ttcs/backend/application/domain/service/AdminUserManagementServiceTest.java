package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.AuditActionType;
import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.ManagedUser;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.admin.GetUsersQuery;
import com.ttcs.backend.application.port.in.admin.ManagedUserPageResult;
import com.ttcs.backend.application.port.in.admin.ManagedUserSummaryResult;
import com.ttcs.backend.application.port.in.admin.SetUserActiveCommand;
import com.ttcs.backend.application.port.in.admin.UpdateUserCommand;
import com.ttcs.backend.application.port.in.admin.UserManagementActionResult;
import com.ttcs.backend.application.port.out.SaveAuditLogPort;
import com.ttcs.backend.application.port.out.admin.ManageUserPort;
import com.ttcs.backend.application.port.out.admin.ManagedUserMetrics;
import com.ttcs.backend.application.port.out.admin.ManagedUserSearchItem;
import com.ttcs.backend.application.port.out.admin.ManagedUserSearchPage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminUserManagementServiceTest {

    @Test
    void shouldListUsers() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminUserManagementService service = new AdminUserManagementService(port, auditLogPort);

        ManagedUserPageResult result = service.getUsers(new GetUsersQuery(null, null, null, null, null, 0, 20, "name", "asc"));

        assertEquals(2, result.items().size());
        assertEquals("admin@university.edu", result.items().get(0).email());
    }

    @Test
    void shouldUpdateTeacherBasicInfo() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminUserManagementService service = new AdminUserManagementService(port, auditLogPort);

        UserManagementActionResult result = service.updateUser(
                new UpdateUserCommand(2, 1, "teacher.updated@university.edu", "Updated Lecturer", 2, null, "T1002")
        );

        assertTrue(result.success());
        assertEquals("USER_UPDATED", result.code());
        assertEquals("teacher.updated@university.edu", port.loadById(2).orElseThrow().getUser().getEmail());
        assertEquals("Updated Lecturer", port.loadById(2).orElseThrow().getName());
        assertEquals(1, auditLogPort.savedLogs.size());
        assertEquals(AuditActionType.USER_PROFILE_UPDATED, auditLogPort.savedLogs.getFirst().getActionType());
        assertTrue(auditLogPort.savedLogs.getFirst().getDetails().contains("email=teacher@university.edu -> teacher.updated@university.edu"));
        assertTrue(auditLogPort.savedLogs.getFirst().getOldState().contains("active=ACTIVE"));
        assertTrue(auditLogPort.savedLogs.getFirst().getNewState().contains("department=Information Systems"));
    }

    @Test
    void shouldBlockDuplicateEmail() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminUserManagementService service = new AdminUserManagementService(port, auditLogPort);

        UserManagementActionResult result = service.updateUser(
                new UpdateUserCommand(2, 1, "admin@university.edu", "Updated Lecturer", 1, null, "T1002")
        );

        assertFalse(result.success());
        assertEquals("EMAIL_ALREADY_USED", result.code());
        assertTrue(auditLogPort.savedLogs.isEmpty());
    }

    @Test
    void shouldDeactivateUser() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminUserManagementService service = new AdminUserManagementService(port, auditLogPort);

        UserManagementActionResult result = service.setUserActive(new SetUserActiveCommand(2, 1, false));

        assertTrue(result.success());
        assertFalse(port.loadById(2).orElseThrow().getUser().getVerified());
        assertEquals(1, auditLogPort.savedLogs.size());
        assertEquals(AuditActionType.USER_DEACTIVATED, auditLogPort.savedLogs.getFirst().getActionType());
        assertEquals("ACTIVE", auditLogPort.savedLogs.getFirst().getOldState());
        assertEquals("INACTIVE", auditLogPort.savedLogs.getFirst().getNewState());
    }

    @Test
    void shouldBlockSelfDeactivation() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminUserManagementService service = new AdminUserManagementService(port, auditLogPort);

        UserManagementActionResult result = service.setUserActive(new SetUserActiveCommand(1, 1, false));

        assertFalse(result.success());
        assertEquals("SELF_ACTION_BLOCKED", result.code());
        assertTrue(auditLogPort.savedLogs.isEmpty());
    }

    @Test
    void shouldRejectUpdateWithoutActorIdAndSkipAudit() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        RecordingAuditLogPort auditLogPort = new RecordingAuditLogPort();
        AdminUserManagementService service = new AdminUserManagementService(port, auditLogPort);

        UserManagementActionResult result = service.updateUser(
                new UpdateUserCommand(2, null, "teacher.updated@university.edu", "Updated Lecturer", 2, null, "T1002")
        );

        assertFalse(result.success());
        assertEquals("INVALID_INPUT", result.code());
        assertTrue(auditLogPort.savedLogs.isEmpty());
    }

    private static final class InMemoryManageUserPort implements ManageUserPort {
        private final List<ManagedUser> users = new ArrayList<>(List.of(
                new ManagedUser(new User(1, "admin@university.edu", "secret", Role.ADMIN, true), "System Admin", null, null, null, null),
                new ManagedUser(new User(2, "teacher@university.edu", "secret", Role.TEACHER, true), "Lecturer Demo", new Department(1, "Computer Science"), null, "T0001", null)
        ));

        @Override
        public List<ManagedUser> loadAll() {
            return List.copyOf(users);
        }

        @Override
        public ManagedUserSearchPage loadPage(com.ttcs.backend.application.port.out.admin.ManageUsersQuery query) {
            List<ManagedUserSearchItem> items = users.stream()
                    .map(user -> new ManagedUserSearchItem(
                            user.getUser().getId(),
                            user.getUser().getEmail(),
                            user.getUser().getRole().name(),
                            user.getName(),
                            user.getDepartment() != null ? user.getDepartment().getId() : null,
                            user.getDepartment() != null ? user.getDepartment().getName() : null,
                            user.getStudentStatus() != null ? user.getStudentStatus().name() : null,
                            Boolean.TRUE.equals(user.getUser().getVerified()),
                            user.getStudentCode(),
                            user.getTeacherCode()
                    ))
                    .toList();

            return new ManagedUserSearchPage(
                    items,
                    0,
                    items.size(),
                    items.size(),
                    1,
                    new ManagedUserMetrics(items.size(), 0, 1, 1, 0, 0)
            );
        }

        @Override
        public List<Department> loadDepartments() {
            return List.of(
                    new Department(1, "Computer Science"),
                    new Department(2, "Information Systems")
            );
        }

        @Override
        public Optional<ManagedUser> loadById(Integer userId) {
            return users.stream().filter(user -> user.getUser().getId().equals(userId)).findFirst();
        }

        @Override
        public Optional<Department> loadDepartmentById(Integer departmentId) {
            if (departmentId == 1) {
                return Optional.of(new Department(1, "Computer Science"));
            }
            if (departmentId == 2) {
                return Optional.of(new Department(2, "Information Systems"));
            }
            return Optional.empty();
        }

        @Override
        public boolean existsByEmailExcludingUserId(String email, Integer userId) {
            return users.stream().anyMatch(user -> user.getUser().getEmail().equals(email) && !user.getUser().getId().equals(userId));
        }

        @Override
        public boolean existsStudentCodeExcludingUserId(String studentCode, Integer userId) {
            return false;
        }

        @Override
        public boolean existsTeacherCodeExcludingUserId(String teacherCode, Integer userId) {
            return users.stream().anyMatch(user -> teacherCode.equals(user.getTeacherCode()) && !user.getUser().getId().equals(userId));
        }

        @Override
        public void save(ManagedUser managedUser) {
            for (int index = 0; index < users.size(); index++) {
                if (users.get(index).getUser().getId().equals(managedUser.getUser().getId())) {
                    users.set(index, managedUser);
                    return;
                }
            }
            users.add(managedUser);
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
