package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.out.auth.LoadStudentByIdPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
                (query, viewerUserId, viewerRole) -> null,
                (surveyId, viewerUserId, viewerRole) -> null,
                exportUseCase,
                new CurrentIdentityProvider(new EmptyStudentPort())
        );

        ResponseEntity<byte[]> response = controller.exportSurveyResult(12);

        byte[] expectedContent = "csv body".getBytes(StandardCharsets.UTF_8);
        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals(expectedContent, response.getBody());
        assertEquals("text/csv;charset=UTF-8", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("attachment"));
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("survey-12-report.csv"));
        assertEquals(12, exportUseCase.surveyId);
        assertEquals(99, exportUseCase.viewerUserId);
        assertEquals(Role.ADMIN, exportUseCase.viewerRole);
    }

    private static final class TrackingExportUseCase implements com.ttcs.backend.application.port.in.resultview.ExportSurveyReportUseCase {
        private Integer surveyId;
        private Integer viewerUserId;
        private Role viewerRole;

        @Override
        public ExportedReport exportSurveyReport(Integer surveyId, Integer viewerUserId, Role viewerRole) {
            this.surveyId = surveyId;
            this.viewerUserId = viewerUserId;
            this.viewerRole = viewerRole;
            return new ExportedReport(
                    "survey-" + surveyId + "-report.csv",
                    "text/csv;charset=UTF-8",
                    "csv body".getBytes(StandardCharsets.UTF_8)
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
