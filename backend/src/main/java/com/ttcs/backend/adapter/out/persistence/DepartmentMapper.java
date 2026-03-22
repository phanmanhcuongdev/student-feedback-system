package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.domain.model.Department;

public class DepartmentMapper {

    public static Department toDomain(DepartmentEntity entity) {
        if (entity == null) return null;

        return new Department(
                entity.getId(),
                entity.getName()
        );
    }

    public static DepartmentEntity toEntity(Department domain) {
        if (domain == null) return null;

        DepartmentEntity entity = new DepartmentEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());

        return entity;
    }
}