package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Student;

import java.util.Optional;

public interface LoadStudentPort {
    Optional<Student> loadById(Integer studentId);
}