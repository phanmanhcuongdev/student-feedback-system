package com.ttcs.backend.adapter.out.persistence.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Integer> {
    @Query("""
            SELECT auditLog
            FROM AuditLogEntity auditLog
            WHERE (:actorUserId IS NULL OR auditLog.actorUserId = :actorUserId)
                AND (:actionType IS NULL OR auditLog.actionType = :actionType)
                AND (:targetType IS NULL OR auditLog.targetType = :targetType)
                AND (:targetId IS NULL OR auditLog.targetId = :targetId)
                AND (:createdFrom IS NULL OR auditLog.createdAt >= :createdFrom)
                AND (:createdTo IS NULL OR auditLog.createdAt < :createdTo)
                AND (
                    :keyword IS NULL
                    OR LOWER(COALESCE(auditLog.summary, '')) LIKE :keyword
                    OR LOWER(COALESCE(auditLog.details, '')) LIKE :keyword
                    OR LOWER(COALESCE(auditLog.oldState, '')) LIKE :keyword
                    OR LOWER(COALESCE(auditLog.newState, '')) LIKE :keyword
                )
            """)
    Page<AuditLogEntity> search(
            @Param("actorUserId") Integer actorUserId,
            @Param("actionType") String actionType,
            @Param("targetType") String targetType,
            @Param("targetId") Integer targetId,
            @Param("createdFrom") java.time.LocalDateTime createdFrom,
            @Param("createdTo") java.time.LocalDateTime createdTo,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
