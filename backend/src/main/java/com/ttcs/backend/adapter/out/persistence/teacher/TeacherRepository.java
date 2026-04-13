package com.ttcs.backend.adapter.out.persistence.teacher;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Integer> {
    boolean existsByTeacherCodeAndIdNot(String teacherCode, Integer id);
}
