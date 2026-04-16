package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.DepartmentOptionResponse;
import com.ttcs.backend.adapter.in.web.dto.ManagedUserDetailResponse;
import com.ttcs.backend.adapter.in.web.dto.ManagedUserMetricsResponse;
import com.ttcs.backend.adapter.in.web.dto.ManagedUserPageResponse;
import com.ttcs.backend.adapter.in.web.dto.ManagedUserSummaryResponse;
import com.ttcs.backend.adapter.in.web.dto.UpdateUserRequest;
import com.ttcs.backend.adapter.in.web.dto.UserManagementActionResponse;
import com.ttcs.backend.application.port.in.admin.GetUserManagementDepartmentsUseCase;
import com.ttcs.backend.application.port.in.admin.GetUsersQuery;
import com.ttcs.backend.application.port.in.admin.GetUserDetailUseCase;
import com.ttcs.backend.application.port.in.admin.GetUsersUseCase;
import com.ttcs.backend.application.port.in.admin.ManagedUserDetailResult;
import com.ttcs.backend.application.port.in.admin.ManagedUserPageResult;
import com.ttcs.backend.application.port.in.admin.ManagedUserSummaryResult;
import com.ttcs.backend.application.port.in.admin.SetUserActiveCommand;
import com.ttcs.backend.application.port.in.admin.SetUserActiveUseCase;
import com.ttcs.backend.application.port.in.admin.UpdateUserCommand;
import com.ttcs.backend.application.port.in.admin.UpdateUserUseCase;
import com.ttcs.backend.application.port.in.admin.UserManagementActionResult;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@WebAdapter
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final GetUsersUseCase getUsersUseCase;
    private final GetUserManagementDepartmentsUseCase getUserManagementDepartmentsUseCase;
    private final GetUserDetailUseCase getUserDetailUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final SetUserActiveUseCase setUserActiveUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @GetMapping
    public ResponseEntity<ManagedUserPageResponse> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String studentStatus,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        ManagedUserPageResult result = getUsersUseCase.getUsers(new GetUsersQuery(
                role,
                keyword,
                active,
                studentStatus,
                departmentId,
                page,
                size,
                sortBy,
                sortDir
        ));
        return ResponseEntity.ok(new ManagedUserPageResponse(
                result.items().stream().map(this::toSummaryResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                new ManagedUserMetricsResponse(
                        result.metrics().totalUsers(),
                        result.metrics().totalStudents(),
                        result.metrics().totalTeachers(),
                        result.metrics().totalAdmins(),
                        result.metrics().totalInactive(),
                        result.metrics().totalPending()
                )
        ));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentOptionResponse>> getDepartmentOptions() {
        return ResponseEntity.ok(getUserManagementDepartmentsUseCase.getDepartments().stream()
                .map(department -> new DepartmentOptionResponse(department.getId(), department.getName()))
                .toList());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ManagedUserDetailResponse> getUserDetail(@PathVariable Integer userId) {
        return ResponseEntity.ok(toDetailResponse(getUserDetailUseCase.getUserDetail(userId)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserManagementActionResponse> updateUser(
            @PathVariable Integer userId,
            @RequestBody UpdateUserRequest request
    ) {
        UserManagementActionResult result = updateUserUseCase.updateUser(
                new UpdateUserCommand(
                        userId,
                        currentStudentProvider.currentUserId(),
                        request.getEmail(),
                        request.getName(),
                        request.getDepartmentId(),
                        request.getStudentCode(),
                        request.getTeacherCode()
                )
        );
        return ResponseEntity.ok(toActionResponse(result));
    }

    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<UserManagementActionResponse> deactivateUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(toActionResponse(setUserActiveUseCase.setUserActive(
                new SetUserActiveCommand(userId, currentStudentProvider.currentUserId(), false)
        )));
    }

    @PostMapping("/{userId}/activate")
    public ResponseEntity<UserManagementActionResponse> activateUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(toActionResponse(setUserActiveUseCase.setUserActive(
                new SetUserActiveCommand(userId, currentStudentProvider.currentUserId(), true)
        )));
    }

    private ManagedUserSummaryResponse toSummaryResponse(ManagedUserSummaryResult result) {
        return new ManagedUserSummaryResponse(
                result.id(),
                result.email(),
                result.role(),
                result.name(),
                result.departmentId(),
                result.departmentName(),
                result.studentStatus(),
                result.active(),
                result.studentCode(),
                result.teacherCode()
        );
    }

    private ManagedUserDetailResponse toDetailResponse(ManagedUserDetailResult result) {
        return new ManagedUserDetailResponse(
                result.id(),
                result.email(),
                result.role(),
                result.active(),
                result.name(),
                result.departmentId(),
                result.departmentName(),
                result.studentCode(),
                result.teacherCode(),
                result.studentStatus()
        );
    }

    private UserManagementActionResponse toActionResponse(UserManagementActionResult result) {
        return new UserManagementActionResponse(result.success(), result.code(), result.message());
    }
}
