package com.ttcs.backend.application.port.out.auth;

import com.ttcs.backend.application.domain.model.StudentToken;

import java.util.Optional;

public interface LoadStudentTokenPort {
    Optional<StudentToken> loadByTokenAndDeleteFlg(String token, Integer deleteFlg);
}
