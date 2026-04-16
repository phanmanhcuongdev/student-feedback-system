package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.AuditLog;

public interface SaveAuditLogPort {
    AuditLog save(AuditLog auditLog);
}
