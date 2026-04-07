package com.ttcs.backend.application.port.out.auth;

import com.ttcs.backend.application.domain.model.Student;

public interface SaveStudentPort {
    Student save(Student student);

    boolean existsByStudentCode(String studentCode);
}
