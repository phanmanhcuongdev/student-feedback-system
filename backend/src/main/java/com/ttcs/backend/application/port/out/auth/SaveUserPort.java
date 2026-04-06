package com.ttcs.backend.application.port.out.auth;

import com.ttcs.backend.application.domain.model.User;

public interface SaveUserPort {
    User save(User user);

    boolean existsByEmail(String email);
}
