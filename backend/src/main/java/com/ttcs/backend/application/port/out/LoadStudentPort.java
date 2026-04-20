package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Student;

import java.util.List;
import java.util.Optional;

public interface LoadStudentPort {
    Optional<Student> loadById(Integer studentId);

    default List<Student> loadByIds(List<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return List.of();
        }
        return studentIds.stream()
                .map(this::loadById)
                .flatMap(Optional::stream)
                .toList();
    }
}
