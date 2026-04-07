package com.ttcs.backend.application.port.out.admin;

import com.ttcs.backend.application.domain.model.Student;

import java.util.List;

public interface LoadPendingStudentsPort {
    List<Student> loadPendingStudents();
}
