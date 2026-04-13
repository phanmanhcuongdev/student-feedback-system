package com.ttcs.backend.application.port.out.auth;

import com.ttcs.backend.application.domain.model.PasswordResetToken;

import java.util.Optional;

public interface LoadPasswordResetTokenPort {
    Optional<PasswordResetToken> loadActiveByTokenHash(String tokenHash);
}
