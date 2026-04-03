package com.ttcs.backend.adapter.out.persistence;


import com.ttcs.backend.application.domain.model.StudentToken;

public class StudentTokenMapper {

    public static StudentToken toDomain(StudentTokenEntity entity) {
        if (entity == null) return null;

        return new StudentToken(
                entity.getId(),
                StudentMapper.toDomain(entity.getStudent()),
                entity.getToken(),
                entity.getCreatedAt(),
                entity.getExpiredAt(),
                entity.getDeleteFlg()
        );
    }

    public static StudentTokenEntity toEntity(StudentToken domain) {
        if (domain == null) return null;

        StudentTokenEntity entity = new StudentTokenEntity();
        entity.setId(domain.getId());
        entity.setStudent(StudentMapper.toEntity(domain.getStudent()));
        entity.setToken(domain.getToken());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiredAt(domain.getExpiredAt());
        entity.setDeleteFlg(domain.getDeleteFlg());

        return entity;
    }
}
