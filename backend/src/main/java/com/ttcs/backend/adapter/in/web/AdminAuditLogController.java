package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.AuditLogPageResponse;
import com.ttcs.backend.adapter.in.web.dto.AuditLogResponse;
import com.ttcs.backend.application.port.in.admin.AuditLogPageResult;
import com.ttcs.backend.application.port.in.admin.AuditLogResult;
import com.ttcs.backend.application.port.in.admin.GetAuditLogsQuery;
import com.ttcs.backend.application.port.in.admin.GetAuditLogsUseCase;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@WebAdapter
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final GetAuditLogsUseCase getAuditLogsUseCase;

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
        AuditLogPageResult result = getAuditLogsUseCase.getAuditLogs(new GetAuditLogsQuery(
                actorUserId,
                actionType,
                targetType,
                targetId,
                keyword,
                createdFrom,
                createdTo,
                page,
                size
        ));

        return ResponseEntity.ok(new AuditLogPageResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    private AuditLogResponse toResponse(AuditLogResult result) {
        return new AuditLogResponse(
                result.id(),
                result.actorUserId(),
                result.actionType(),
                result.targetType(),
                result.targetId(),
                result.summary(),
                result.details(),
                result.oldState(),
                result.newState(),
                result.createdAt()
        );
    }
}
