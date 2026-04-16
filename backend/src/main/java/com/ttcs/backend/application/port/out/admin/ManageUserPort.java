package com.ttcs.backend.application.port.out.admin;

import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.ManagedUser;

import java.util.List;
import java.util.Optional;

public interface ManageUserPort {
    List<ManagedUser> loadAll();

    ManagedUserSearchPage loadPage(ManageUsersQuery query);

    List<Department> loadDepartments();

    Optional<ManagedUser> loadById(Integer userId);

    Optional<Department> loadDepartmentById(Integer departmentId);

    boolean existsByEmailExcludingUserId(String email, Integer userId);

    boolean existsStudentCodeExcludingUserId(String studentCode, Integer userId);

    boolean existsTeacherCodeExcludingUserId(String teacherCode, Integer userId);

    void save(ManagedUser managedUser);
}
