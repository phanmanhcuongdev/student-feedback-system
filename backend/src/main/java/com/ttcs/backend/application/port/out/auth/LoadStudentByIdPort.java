package com.ttcs.backend.application.port.out.auth;

import com.ttcs.backend.application.domain.model.Student;

import java.util.Optional;

public interface LoadStudentByIdPort {
    Optional<Student> loadById(Integer studentId);

    Optional<Student> loadByUserId(Integer userId);
}
