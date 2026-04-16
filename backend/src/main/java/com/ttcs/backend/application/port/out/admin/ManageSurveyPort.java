package com.ttcs.backend.application.port.out.admin;

import com.ttcs.backend.application.domain.model.Department;

import java.util.List;

public interface ManageSurveyPort {
    ManagedSurveySearchPage loadPage(ManageSurveysQuery query);

    List<Department> loadDepartments();
}
