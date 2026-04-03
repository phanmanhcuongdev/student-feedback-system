package com.ttcs.backend.adapter.out.persistence.user;

import com.ttcs.backend.adapter.out.persistence.role.RoleMapper;
import com.ttcs.backend.application.domain.model.User;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;

        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                RoleMapper.toDomain(entity.getRole()),
                entity.getVerified()
        );
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) return null;

        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setRole(RoleMapper.toEntity(domain.getRole()));
        entity.setVerified(domain.getVerified());

        return entity;
    }
}