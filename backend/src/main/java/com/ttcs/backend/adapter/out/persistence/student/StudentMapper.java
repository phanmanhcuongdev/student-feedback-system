package com.ttcs.backend.adapter.out.persistence.student;

import com.ttcs.backend.adapter.out.persistence.department.DepartmentMapper;
import com.ttcs.backend.adapter.out.persistence.user.UserMapper;
import com.ttcs.backend.application.domain.model.Student;

public class StudentMapper {

    public static Student toDomain(StudentEntity entity) {
        if (entity == null) return null;

        return new Student(
                entity.getId(),
                UserMapper.toDomain(entity.getUser()),
                entity.getName(),
                entity.getStudentCode(),
                DepartmentMapper.toDomain(entity.getDepartment()),
                StatusMapper.toDomain(entity.getStatus()),
                entity.getStudentCardImageUrl(),
                entity.getNationalIdImageUrl()
        );
    }

    public static StudentEntity toEntity(Student domain) {
        if (domain == null) return null;

        StudentEntity entity = new StudentEntity();
        entity.setId(domain.getId());
        entity.setUser(UserMapper.toEntity(domain.getUser()));
        entity.setName(domain.getName());
        entity.setStudentCode(domain.getStudentCode());
        entity.setDepartment(DepartmentMapper.toEntity(domain.getDepartment()));
        entity.setStatus(StatusMapper.toEntity(domain.getStatus()));
        entity.setNationalIdImageUrl(domain.getNationalIdImageUrl());
        entity.setStudentCardImageUrl(domain.getStudentCardImageUrl());

        return entity;
    }
}