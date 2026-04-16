package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Student;

import java.util.List;

public interface LoadSurveyRecipientCandidatePort {
    List<Student> loadActiveStudents();

    List<Student> loadActiveStudentsByDepartment(Integer departmentId);
}
