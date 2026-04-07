package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.domain.model.Status;

public class StatusMapper {

    public static Status toDomain(StatusEntity entity) {
        if (entity == null) return null;
        return Status.valueOf(entity.name());
    }

    public static StatusEntity toEntity(Status domain) {
        if (domain == null) return null;
        return StatusEntity.valueOf(domain.name());
    }
}
