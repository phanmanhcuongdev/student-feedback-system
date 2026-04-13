package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PasswordResetToken {
    private final Integer id;
    private final User user;
    private final String tokenHash;
    private final LocalDateTime expiredAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime usedAt;
}
