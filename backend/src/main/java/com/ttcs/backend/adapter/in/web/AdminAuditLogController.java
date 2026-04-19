package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.AuditLogPageResponse;
import com.ttcs.backend.adapter.in.web.dto.AuditLogResponse;
import com.ttcs.backend.adapter.out.persistence.auditlog.AuditLogEntity;
import com.ttcs.backend.adapter.out.persistence.auditlog.AuditLogRepository;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@WebAdapter
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<AuditLogPageResponse> getAuditLogs(
            @RequestParam(required = false) Integer actorUserId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Integer targetId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<AuditLogEntity> results = auditLogRepository.search(
                actorUserId,
                normalizeExact(actionType),
                normalizeExact(targetType),
                targetId,
                createdFrom == null ? null : createdFrom.atStartOfDay(),
                createdTo == null ? null : createdTo.plusDays(1).atStartOfDay(),
                normalizeKeyword(keyword),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")))
        );

        return ResponseEntity.ok(new AuditLogPageResponse(
                results.getContent().stream().map(this::toResponse).toList(),
                results.getNumber(),
                results.getSize(),
                results.getTotalElements(),
                results.getTotalPages()
        ));
    }

    private AuditLogResponse toResponse(AuditLogEntity entity) {
        LocalDateTime createdAt = entity.getCreatedAt();
        return new AuditLogResponse(
                entity.getId(),
                entity.getActorUserId(),
                entity.getActionType(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getSummary(),
                entity.getDetails(),
                entity.getOldState(),
                entity.getNewState(),
                createdAt
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
