package com.ttcs.backend.adapter.out.persistence.student;

import com.ttcs.backend.adapter.out.persistence.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<StudentEntity, Integer> {
    boolean existsByStudentCode(String studentCode);

    Optional<StudentEntity> findByUserId(Integer userId);

    List<StudentEntity> findByStatusOrderByIdAsc(StatusEntity status);

    List<StudentEntity> findByStatusAndUser_VerifiedTrueOrderByIdAsc(StatusEntity status);

    List<StudentEntity> findByStatusAndDepartment_IdAndUser_VerifiedTrueOrderByIdAsc(StatusEntity status, Integer departmentId);
}
