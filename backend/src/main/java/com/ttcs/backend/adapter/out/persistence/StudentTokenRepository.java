package com.ttcs.backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentTokenRepository extends JpaRepository<StudentTokenEntity, Integer> {
    Optional<StudentTokenEntity> findByTokenAndDeleteFlg(String token, Integer deleteFlg);
}
