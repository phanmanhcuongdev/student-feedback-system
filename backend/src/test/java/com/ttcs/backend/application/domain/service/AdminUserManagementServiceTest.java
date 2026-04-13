package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.ManagedUser;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.admin.ManagedUserSummaryResult;
import com.ttcs.backend.application.port.in.admin.SetUserActiveCommand;
import com.ttcs.backend.application.port.in.admin.UpdateUserCommand;
import com.ttcs.backend.application.port.in.admin.UserManagementActionResult;
import com.ttcs.backend.application.port.out.admin.ManageUserPort;
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
        AdminUserManagementService service = new AdminUserManagementService(port);

        List<ManagedUserSummaryResult> result = service.getUsers();

        assertEquals(2, result.size());
        assertEquals("admin@university.edu", result.get(0).email());
    }

    @Test
    void shouldUpdateTeacherBasicInfo() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        AdminUserManagementService service = new AdminUserManagementService(port);

        UserManagementActionResult result = service.updateUser(
                new UpdateUserCommand(2, "teacher.updated@university.edu", "Updated Lecturer", 2, null, "T1002")
        );

        assertTrue(result.success());
        assertEquals("USER_UPDATED", result.code());
        assertEquals("teacher.updated@university.edu", port.loadById(2).orElseThrow().getUser().getEmail());
        assertEquals("Updated Lecturer", port.loadById(2).orElseThrow().getName());
    }

    @Test
    void shouldBlockDuplicateEmail() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        AdminUserManagementService service = new AdminUserManagementService(port);

        UserManagementActionResult result = service.updateUser(
                new UpdateUserCommand(2, "admin@university.edu", "Updated Lecturer", 1, null, "T1002")
        );

        assertFalse(result.success());
        assertEquals("EMAIL_ALREADY_USED", result.code());
    }

    @Test
    void shouldDeactivateUser() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        AdminUserManagementService service = new AdminUserManagementService(port);

        UserManagementActionResult result = service.setUserActive(new SetUserActiveCommand(2, 1, false));

        assertTrue(result.success());
        assertFalse(port.loadById(2).orElseThrow().getUser().getVerified());
    }

    @Test
    void shouldBlockSelfDeactivation() {
        InMemoryManageUserPort port = new InMemoryManageUserPort();
        AdminUserManagementService service = new AdminUserManagementService(port);

        UserManagementActionResult result = service.setUserActive(new SetUserActiveCommand(1, 1, false));

        assertFalse(result.success());
        assertEquals("SELF_ACTION_BLOCKED", result.code());
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
}
