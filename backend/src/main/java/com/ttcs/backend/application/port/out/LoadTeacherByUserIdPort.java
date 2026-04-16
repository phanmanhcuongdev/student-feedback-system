package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Teacher;

import java.util.Optional;

public interface LoadTeacherByUserIdPort {
    Optional<Teacher> loadByUserId(Integer userId);
}
