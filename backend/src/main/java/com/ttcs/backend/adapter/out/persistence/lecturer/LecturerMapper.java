package com.ttcs.backend.adapter.out.persistence.lecturer;

import com.ttcs.backend.adapter.out.persistence.department.DepartmentEntity;
import com.ttcs.backend.adapter.out.persistence.department.DepartmentMapper;
import com.ttcs.backend.adapter.out.persistence.user.UserEntity;
import com.ttcs.backend.adapter.out.persistence.user.UserMapper;
import com.ttcs.backend.application.domain.model.Lecturer;

public class LecturerMapper {

    public static Lecturer toDomain(LecturerEntity entity) {
        if (entity == null) return null;

        return new Lecturer(
                entity.getId(),
                UserMapper.toDomain(entity.getUser()),
                entity.getName(),
                entity.getLecturerCode(),
                DepartmentMapper.toDomain(entity.getDepartment())
        );
    }

    public static LecturerEntity toEntity(Lecturer domain) {
        if (domain == null) return null;

        LecturerEntity entity = new LecturerEntity();
        entity.setId(domain.getId());

        // shallow user
        UserEntity user = new UserEntity();
        user.setId(domain.getUser().getId());
        entity.setUser(user);

        entity.setName(domain.getName());
        entity.setLecturerCode(domain.getLecturerCode());

        // shallow department
        DepartmentEntity dept = new DepartmentEntity();
        dept.setId(domain.getDepartment().getId());
        entity.setDepartment(dept);

        return entity;
    }
}