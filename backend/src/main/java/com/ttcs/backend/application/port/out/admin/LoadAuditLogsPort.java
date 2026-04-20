package com.ttcs.backend.application.port.out.admin;

public interface LoadAuditLogsPort {
    AuditLogSearchPage loadPage(AuditLogSearchQuery query);
}
