package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.port.in.admin.AuditLogPageResult;
import com.ttcs.backend.application.port.in.admin.AuditLogResult;
import com.ttcs.backend.application.port.in.admin.GetAuditLogsQuery;
import com.ttcs.backend.application.port.in.admin.GetAuditLogsUseCase;
import com.ttcs.backend.application.port.out.admin.AuditLogSearchQuery;
import com.ttcs.backend.application.port.out.admin.LoadAuditLogsPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class AuditLogQueryService implements GetAuditLogsUseCase {

    private final LoadAuditLogsPort loadAuditLogsPort;

    @Override
    @Transactional(readOnly = true)
    public AuditLogPageResult getAuditLogs(GetAuditLogsQuery query) {
        var page = loadAuditLogsPort.loadPage(new AuditLogSearchQuery(
                query == null ? null : query.actorUserId(),
                normalizeExact(query == null ? null : query.actionType()),
                normalizeExact(query == null ? null : query.targetType()),
                query == null ? null : query.targetId(),
                query == null || query.createdFrom() == null ? null : query.createdFrom().atStartOfDay(),
                query == null || query.createdTo() == null ? null : query.createdTo().plusDays(1).atStartOfDay(),
                normalizeKeyword(query == null ? null : query.keyword()),
                query == null ? 0 : query.page(),
                query == null ? 20 : query.size()
        ));

        return new AuditLogPageResult(
                page.items().stream().map(this::toResult).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    private AuditLogResult toResult(AuditLog auditLog) {
        return new AuditLogResult(
                auditLog.getId(),
                auditLog.getActorUserId(),
                auditLog.getActionType().name(),
                auditLog.getTargetType().name(),
                auditLog.getTargetId(),
                auditLog.getSummary(),
                auditLog.getDetails(),
                auditLog.getOldState(),
                auditLog.getNewState(),
                auditLog.getCreatedAt()
        );
    }

    private String normalizeExact(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private String normalizeKeyword(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + value.trim().toLowerCase() + "%";
    }
}
