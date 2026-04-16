package com.ttcs.backend.adapter.out.persistence.auditlog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Integer> {
}
