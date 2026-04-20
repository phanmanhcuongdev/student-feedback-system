package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.AuditActionType;
import com.ttcs.backend.application.domain.model.AuditLog;
import com.ttcs.backend.application.domain.model.AuditTargetType;
import com.ttcs.backend.application.port.in.admin.GetAuditLogsQuery;
import com.ttcs.backend.application.port.out.admin.AuditLogSearchPage;
import com.ttcs.backend.application.port.out.admin.AuditLogSearchQuery;
import com.ttcs.backend.application.port.out.admin.LoadAuditLogsPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditLogQueryServiceTest {

    @Test
    void shouldNormalizeFiltersAndMapResults() {
        RecordingLoadAuditLogsPort port = new RecordingLoadAuditLogsPort();
        AuditLogQueryService service = new AuditLogQueryService(port);

        var result = service.getAuditLogs(new GetAuditLogsQuery(
                10,
                " user_deactivated ",
                " user ",
                20,
                " Changed ",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 20),
                -1,
                500
        ));

        assertEquals("USER_DEACTIVATED", port.lastQuery.actionType());
        assertEquals("USER", port.lastQuery.targetType());
        assertEquals("%changed%", port.lastQuery.keyword());
        assertEquals(LocalDateTime.of(2026, 4, 1, 0, 0), port.lastQuery.createdFrom());
        assertEquals(LocalDateTime.of(2026, 4, 21, 0, 0), port.lastQuery.createdTo());
        assertEquals(1, result.items().size());
        assertEquals("USER_DEACTIVATED", result.items().getFirst().actionType());
    }

    @Test
    void shouldUseDefaultsForNullQuery() {
        RecordingLoadAuditLogsPort port = new RecordingLoadAuditLogsPort();
        AuditLogQueryService service = new AuditLogQueryService(port);

        service.getAuditLogs(null);

        assertEquals(0, port.lastQuery.page());
        assertEquals(20, port.lastQuery.size());
    }

    private static final class RecordingLoadAuditLogsPort implements LoadAuditLogsPort {
        private AuditLogSearchQuery lastQuery;

        @Override
        public AuditLogSearchPage loadPage(AuditLogSearchQuery query) {
            lastQuery = query;
            AuditLog log = new AuditLog(
                    1,
                    10,
                    AuditActionType.USER_DEACTIVATED,
                    AuditTargetType.USER,
                    20,
                    "Deactivated user",
                    "email=user@example.edu",
                    "ACTIVE",
                    "INACTIVE",
                    LocalDateTime.of(2026, 4, 20, 12, 0)
            );
            return new AuditLogSearchPage(List.of(log), 0, 20, 1, 1);
        }
    }
}
