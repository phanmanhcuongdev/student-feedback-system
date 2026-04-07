package com.ttcs.backend.application.port.out.auth;

import com.ttcs.backend.application.domain.model.Department;

import java.util.Optional;

public interface LoadDepartmentPort {
    Optional<Department> loadByName(String departmentName);
}
