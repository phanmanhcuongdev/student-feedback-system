package com.ttcs.backend.adapter.out.persistence.lecturer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LecturerRepository extends JpaRepository<LecturerEntity, Integer> {
    boolean existsByLecturerCodeAndIdNot(String lecturerCode, Integer id);
}
