package com.ttcs.backend.adapter.out.persistence.auditlog;

import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.domain.model.AuditActionType;
import com.ttcs.backend.application.domain.model.AuditTargetType;

public final class AuditLogMapper {
    private AuditLogMapper() {
    }

    public static AuditLog toDomain(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AuditLog(
                entity.getId(),
                entity.getActorUserId(),
                AuditActionType.valueOf(entity.getActionType()),
                AuditTargetType.valueOf(entity.getTargetType()),
                entity.getTargetId(),
                entity.getSummary(),
                entity.getDetails(),
                entity.getOldState(),
                entity.getNewState(),
                entity.getCreatedAt()
        );
    }

    public static AuditLogEntity toEntity(AuditLog domain) {
        if (domain == null) {
            return null;
        }
        return new AuditLogEntity(
                domain.getId(),
                domain.getActorUserId(),
                domain.getActionType().name(),
                domain.getTargetType().name(),
                domain.getTargetId(),
                domain.getSummary(),
                domain.getDetails(),
                domain.getOldState(),
                domain.getNewState(),
                domain.getCreatedAt()
        );
    }
}
