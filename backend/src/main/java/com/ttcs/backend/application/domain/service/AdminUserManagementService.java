package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.AuditActionType;
import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.domain.model.AuditTargetType;
import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.ManagedUser;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.admin.GetUserDetailUseCase;
import com.ttcs.backend.application.port.in.admin.GetUserManagementDepartmentsUseCase;
import com.ttcs.backend.application.port.in.admin.GetUsersQuery;
import com.ttcs.backend.application.port.in.admin.GetUsersUseCase;
import com.ttcs.backend.application.port.in.admin.ManagedUserDetailResult;
import com.ttcs.backend.application.port.in.admin.ManagedUserMetricsResult;
import com.ttcs.backend.application.port.in.admin.ManagedUserPageResult;
import com.ttcs.backend.application.port.in.admin.ManagedUserSummaryResult;
import com.ttcs.backend.application.port.in.admin.SetUserActiveCommand;
import com.ttcs.backend.application.port.in.admin.SetUserActiveUseCase;
import com.ttcs.backend.application.port.in.admin.UpdateUserCommand;
import com.ttcs.backend.application.port.in.admin.UpdateUserUseCase;
import com.ttcs.backend.application.port.in.admin.UserManagementActionResult;
import com.ttcs.backend.application.port.out.admin.ManagedUserSearchItem;
import com.ttcs.backend.application.port.out.admin.ManagedUserSearchPage;
import com.ttcs.backend.application.port.out.admin.ManageUsersQuery;
import com.ttcs.backend.application.port.out.SaveAuditLogPort;
import com.ttcs.backend.application.port.out.admin.ManageUserPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class AdminUserManagementService implements
        GetUsersUseCase,
        GetUserManagementDepartmentsUseCase,
        GetUserDetailUseCase,
        UpdateUserUseCase,
        SetUserActiveUseCase {

    private final ManageUserPort manageUserPort;
    private final SaveAuditLogPort saveAuditLogPort;

    @Override
    @Transactional(readOnly = true)
    public ManagedUserPageResult getUsers(GetUsersQuery query) {
        ManagedUserSearchPage page = manageUserPort.loadPage(new ManageUsersQuery(
                query != null ? query.role() : null,
                query != null ? query.keyword() : null,
                query != null ? query.active() : null,
                query != null ? query.studentStatus() : null,
                query != null ? query.departmentId() : null,
                query != null ? query.page() : 0,
                query != null ? query.size() : 20,
                query != null ? query.sortBy() : "name",
                query != null ? query.sortDir() : "asc"
        ));

        return new ManagedUserPageResult(
                page.items().stream().map(this::toSummary).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                new ManagedUserMetricsResult(
                        page.metrics().totalUsers(),
                        page.metrics().totalStudents(),
                        page.metrics().totalTeachers(),
                        page.metrics().totalAdmins(),
                        page.metrics().totalInactive(),
                        page.metrics().totalPending()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getDepartments() {
        return manageUserPort.loadDepartments();
    }

    @Override
    @Transactional(readOnly = true)
    public ManagedUserDetailResult getUserDetail(Integer userId) {
        ManagedUser user = manageUserPort.loadById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        return toDetail(user);
    }

    @Override
    @Transactional
    public UserManagementActionResult updateUser(UpdateUserCommand command) {
        if (command == null
                || command.userId() == null
                || command.actorUserId() == null
                || isBlank(command.email())
                || isBlank(command.name())) {
            return UserManagementActionResult.fail("INVALID_INPUT", "Actor, email, and name are required.");
        }

        ManagedUser existing = manageUserPort.loadById(command.userId()).orElse(null);
        if (existing == null) {
            return UserManagementActionResult.fail("USER_NOT_FOUND", "User was not found.");
        }

        String email = command.email().trim();
        if (manageUserPort.existsByEmailExcludingUserId(email, command.userId())) {
            return UserManagementActionResult.fail("EMAIL_ALREADY_USED", "Email is already used by another user.");
        }

        Department department = existing.getDepartment();
        String studentCode = existing.getStudentCode();
        String teacherCode = existing.getTeacherCode();

        if (existing.getUser().getRole() == Role.STUDENT) {
            if (command.departmentId() == null || isBlank(command.studentCode())) {
                return UserManagementActionResult.fail("INVALID_INPUT", "Student department and student code are required.");
            }
            department = manageUserPort.loadDepartmentById(command.departmentId()).orElse(null);
            if (department == null) {
                return UserManagementActionResult.fail("DEPARTMENT_NOT_FOUND", "Department was not found.");
            }
            studentCode = command.studentCode().trim();
            if (manageUserPort.existsStudentCodeExcludingUserId(studentCode, command.userId())) {
                return UserManagementActionResult.fail("STUDENT_CODE_ALREADY_USED", "Student code is already used by another user.");
            }
        } else if (existing.getUser().getRole() == Role.TEACHER) {
            if (command.departmentId() == null || isBlank(command.teacherCode())) {
                return UserManagementActionResult.fail("INVALID_INPUT", "Teacher department and teacher code are required.");
            }
            department = manageUserPort.loadDepartmentById(command.departmentId()).orElse(null);
            if (department == null) {
                return UserManagementActionResult.fail("DEPARTMENT_NOT_FOUND", "Department was not found.");
            }
            teacherCode = command.teacherCode().trim();
            if (manageUserPort.existsTeacherCodeExcludingUserId(teacherCode, command.userId())) {
                return UserManagementActionResult.fail("TEACHER_CODE_ALREADY_USED", "Teacher code is already used by another user.");
            }
        }

        ManagedUser updated = new ManagedUser(
                new User(
                        existing.getUser().getId(),
                        email,
                        existing.getUser().getPassword(),
                        existing.getUser().getRole(),
                        existing.getUser().getVerified()
                ),
                command.name().trim(),
                department,
                studentCode,
                teacherCode,
                existing.getStudentStatus()
        );
        manageUserPort.save(updated);
        saveAuditLogPort.save(new AuditLog(
                null,
                command.actorUserId(),
                AuditActionType.USER_PROFILE_UPDATED,
                AuditTargetType.USER,
                existing.getUser().getId(),
                "Updated managed user profile",
                buildUserUpdateDetails(existing, updated),
                userStateSummary(existing),
                userStateSummary(updated),
                null
        ));
        return UserManagementActionResult.ok("USER_UPDATED", "User updated successfully.");
    }

    @Override
    @Transactional
    public UserManagementActionResult setUserActive(SetUserActiveCommand command) {
        if (command == null || command.targetUserId() == null || command.actorUserId() == null) {
            return UserManagementActionResult.fail("INVALID_INPUT", "User action is invalid.");
        }
        if (command.targetUserId().equals(command.actorUserId())) {
            return UserManagementActionResult.fail("SELF_ACTION_BLOCKED", "You cannot change the active state of your own account.");
        }

        ManagedUser existing = manageUserPort.loadById(command.targetUserId()).orElse(null);
        if (existing == null) {
            return UserManagementActionResult.fail("USER_NOT_FOUND", "User was not found.");
        }

        ManagedUser updated = new ManagedUser(
                new User(
                        existing.getUser().getId(),
                        existing.getUser().getEmail(),
                        existing.getUser().getPassword(),
                        existing.getUser().getRole(),
                        command.active()
                ),
                existing.getName(),
                existing.getDepartment(),
                existing.getStudentCode(),
                existing.getTeacherCode(),
                existing.getStudentStatus()
        );
        manageUserPort.save(updated);
        saveAuditLogPort.save(new AuditLog(
                null,
                command.actorUserId(),
                command.active() ? AuditActionType.USER_ACTIVATED : AuditActionType.USER_DEACTIVATED,
                AuditTargetType.USER,
                existing.getUser().getId(),
                command.active() ? "Activated user account" : "Deactivated user account",
                "role=" + existing.getUser().getRole().name() + "; email=" + existing.getUser().getEmail(),
                activeState(Boolean.TRUE.equals(existing.getUser().getVerified())),
                activeState(command.active()),
                null
        ));

        return command.active()
                ? UserManagementActionResult.ok("USER_ACTIVATED", "User activated successfully.")
                : UserManagementActionResult.ok("USER_DEACTIVATED", "User deactivated successfully.");
    }

    private ManagedUserSummaryResult toSummary(ManagedUser user) {
        return new ManagedUserSummaryResult(
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
        );
    }

    private ManagedUserSummaryResult toSummary(ManagedUserSearchItem user) {
        return new ManagedUserSummaryResult(
                user.id(),
                user.email(),
                user.role(),
                user.name(),
                user.departmentId(),
                user.departmentName(),
                user.studentStatus(),
                user.active(),
                user.studentCode(),
                user.teacherCode()
        );
    }

    private ManagedUserDetailResult toDetail(ManagedUser user) {
        return new ManagedUserDetailResult(
                user.getUser().getId(),
                user.getUser().getEmail(),
                user.getUser().getRole().name(),
                Boolean.TRUE.equals(user.getUser().getVerified()),
                user.getName(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getStudentCode(),
                user.getTeacherCode(),
                user.getStudentStatus() != null ? user.getStudentStatus().name() : null
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String buildUserUpdateDetails(ManagedUser before, ManagedUser after) {
        StringBuilder details = new StringBuilder();
        details.append("role=").append(after.getUser().getRole().name());
        appendFieldChange(details, "email", before.getUser().getEmail(), after.getUser().getEmail());
        appendFieldChange(details, "name", before.getName(), after.getName());
        appendFieldChange(
                details,
                "department",
                before.getDepartment() != null ? before.getDepartment().getName() : null,
                after.getDepartment() != null ? after.getDepartment().getName() : null
        );
        if (after.getStudentCode() != null || before.getStudentCode() != null) {
            appendFieldChange(details, "studentCode", before.getStudentCode(), after.getStudentCode());
        }
        if (after.getTeacherCode() != null || before.getTeacherCode() != null) {
            appendFieldChange(details, "teacherCode", before.getTeacherCode(), after.getTeacherCode());
        }
        return details.toString();
    }

    private String userStateSummary(ManagedUser user) {
        return "email=" + user.getUser().getEmail()
                + ", name=" + user.getName()
                + ", department=" + (user.getDepartment() != null ? user.getDepartment().getName() : "-")
                + ", active=" + activeState(Boolean.TRUE.equals(user.getUser().getVerified()));
    }

    private void appendFieldChange(StringBuilder details, String field, String before, String after) {
        details.append("; ")
                .append(field)
                .append("=")
                .append(before == null ? "-" : before)
                .append(" -> ")
                .append(after == null ? "-" : after);
    }

    private String activeState(boolean active) {
        return active ? "ACTIVE" : "INACTIVE";
    }
}
