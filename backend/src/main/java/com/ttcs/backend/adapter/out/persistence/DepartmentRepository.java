package com.ttcs.backend.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Integer> {

    Optional<DepartmentEntity> findByName(String departmentName);
}
