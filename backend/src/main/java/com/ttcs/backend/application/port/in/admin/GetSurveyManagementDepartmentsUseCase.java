package com.ttcs.backend.application.port.in.admin;

import com.ttcs.backend.application.domain.model.Department;

import java.util.List;

public interface GetSurveyManagementDepartmentsUseCase {
    List<Department> getDepartments();
}
