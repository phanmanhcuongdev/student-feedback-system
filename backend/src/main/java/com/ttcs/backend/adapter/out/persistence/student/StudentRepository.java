package com.ttcs.backend.adapter.out.persistence.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<StudentEntity, Integer> {
    boolean existsByStudentCode(String studentCode);

    Optional<StudentEntity> findByUserId(Integer userId);
}