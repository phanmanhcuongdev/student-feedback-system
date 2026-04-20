package com.ttcs.backend.adapter.out.persistence.auditlog;

import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.port.out.SaveAuditLogPort;
import com.ttcs.backend.application.port.out.admin.AuditLogSearchPage;
import com.ttcs.backend.application.port.out.admin.AuditLogSearchQuery;
import com.ttcs.backend.application.port.out.admin.LoadAuditLogsPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@PersistenceAdapter
@RequiredArgsConstructor
public class AuditLogPersistenceAdapter implements SaveAuditLogPort, LoadAuditLogsPort {

    private final AuditLogRepository auditLogRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return AuditLogMapper.toDomain(auditLogRepository.save(AuditLogMapper.toEntity(auditLog)));
    }

    @Override
    public AuditLogSearchPage loadPage(AuditLogSearchQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.min(Math.max(query.size(), 1), 100);
        var results = auditLogRepository.search(
                query.actorUserId(),
                query.actionType(),
                query.targetType(),
                query.targetId(),
                query.createdFrom(),
                query.createdTo(),
                query.keyword(),
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
                )
        );

        return new AuditLogSearchPage(
                results.getContent().stream().map(AuditLogMapper::toDomain).toList(),
                results.getNumber(),
                results.getSize(),
                results.getTotalElements(),
                results.getTotalPages()
        );
    }
}
