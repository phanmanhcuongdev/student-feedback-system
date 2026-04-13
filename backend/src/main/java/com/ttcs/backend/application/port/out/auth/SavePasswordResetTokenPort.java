package com.ttcs.backend.application.port.out.auth;

import com.ttcs.backend.application.domain.model.PasswordResetToken;

public interface SavePasswordResetTokenPort {
    PasswordResetToken save(PasswordResetToken token);

    void markActiveTokensUsed(Integer userId);
}
