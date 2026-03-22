package com.ttcs.backend.adapter.out.persistence;


import com.ttcs.backend.application.domain.model.Role;

public class RoleMapper {

    public static Role toDomain(RoleEntity entity) {
        if (entity == null) return null;
        return Role.valueOf(entity.name());
    }

    public static RoleEntity toEntity(Role domain) {
        if (domain == null) return null;
        return RoleEntity.valueOf(domain.name());
    }
}