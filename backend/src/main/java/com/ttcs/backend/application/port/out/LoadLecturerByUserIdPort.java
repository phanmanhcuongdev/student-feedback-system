package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Lecturer;

import java.util.Optional;

public interface LoadLecturerByUserIdPort {
    Optional<Lecturer> loadByUserId(Integer userId);
}
