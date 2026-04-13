package com.ttcs.backend.application.port.out.auth;

import com.ttcs.backend.application.domain.model.User;

import java.util.Optional;

public interface LoadUserByIdPort {
    Optional<User> loadById(Integer userId);
}
