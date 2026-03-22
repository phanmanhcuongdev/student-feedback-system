package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.domain.model.Teacher;

public class TeacherMapper {

    public static Teacher toDomain(TeacherEntity entity) {
        if (entity == null) return null;

        return new Teacher(
                entity.getId(),
                UserMapper.toDomain(entity.getUser()),
                entity.getName(),
                entity.getTeacherCode(),
                DepartmentMapper.toDomain(entity.getDepartment())
        );
    }

    public static TeacherEntity toEntity(Teacher domain) {
        if (domain == null) return null;

        TeacherEntity entity = new TeacherEntity();
        entity.setId(domain.getId());

        // shallow user
        UserEntity user = new UserEntity();
        user.setId(domain.getUser().getId());
        entity.setUser(user);

        entity.setName(domain.getName());
        entity.setTeacherCode(domain.getTeacherCode());

        // shallow department
        DepartmentEntity dept = new DepartmentEntity();
        dept.setId(domain.getDepartment().getId());
        entity.setDepartment(dept);

        return entity;
    }
}