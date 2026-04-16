package com.ttcs.backend.adapter.out.persistence.auditlog;

import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.port.out.SaveAuditLogPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class AuditLogPersistenceAdapter implements SaveAuditLogPort {

    private final AuditLogRepository auditLogRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return AuditLogMapper.toDomain(auditLogRepository.save(AuditLogMapper.toEntity(auditLog)));
    }
}
