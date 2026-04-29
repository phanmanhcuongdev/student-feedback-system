package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurveyResultControllerTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnExportedReportWithAttachmentHeaders() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                99,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
        TrackingExportUseCase exportUseCase = new TrackingExportUseCase();
        SurveyResultController controller = new SurveyResultController(
                (query, viewerUserId, viewerRole, targetLang) -> null,
                (surveyId, viewerUserId, viewerRole, targetLang) -> null,
                exportUseCase,
                new CurrentIdentityProvider(new EmptyStudentPort())
        );

        ResponseEntity<ByteArrayResource> response = controller.exportSurveyResult(12, "xlsx");

        byte[] expectedContent = "report body".getBytes(StandardCharsets.UTF_8);
        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals(expectedContent, response.getBody().getByteArray());
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("attachment"));
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("survey-report.xlsx"));
        assertEquals(12, exportUseCase.surveyId);
        assertEquals(99, exportUseCase.viewerUserId);
        assertEquals(Role.ADMIN, exportUseCase.viewerRole);
        assertEquals("xlsx", exportUseCase.format);
    }

    private static final class TrackingExportUseCase implements com.ttcs.backend.application.port.in.resultview.ExportSurveyReportUseCase {
        private Integer surveyId;
        private Integer viewerUserId;
        private Role viewerRole;
        private String format;

        @Override
        public ExportedReport exportSurveyReport(Integer surveyId, Integer viewerUserId, Role viewerRole, String format) {
            this.surveyId = surveyId;
            this.viewerUserId = viewerUserId;
            this.viewerRole = viewerRole;
            this.format = format;
            return new ExportedReport(
                    "survey-report." + format,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "report body".getBytes(StandardCharsets.UTF_8)
            );
        }
    }

    private static final class EmptyStudentPort implements LoadStudentByIdPort {
        @Override
        public Optional<Student> loadById(Integer studentId) {
            return Optional.empty();
        }

        @Override
        public Optional<Student> loadByUserId(Integer userId) {
            return Optional.empty();
        }
    }
}
