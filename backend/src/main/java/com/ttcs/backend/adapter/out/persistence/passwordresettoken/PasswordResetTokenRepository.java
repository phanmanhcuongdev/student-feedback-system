package com.ttcs.backend.adapter.out.persistence.passwordresettoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Integer> {
    Optional<PasswordResetTokenEntity> findByTokenHashAndUsedAtIsNull(String tokenHash);

    @Modifying
    @Query("""
            update PasswordResetTokenEntity token
            set token.usedAt = :usedAt
            where token.user.id = :userId
              and token.usedAt is null
            """)
    void markActiveTokensUsed(@Param("userId") Integer userId, @Param("usedAt") LocalDateTime usedAt);
}
