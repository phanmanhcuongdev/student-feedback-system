package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.adapter.out.persistence.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
