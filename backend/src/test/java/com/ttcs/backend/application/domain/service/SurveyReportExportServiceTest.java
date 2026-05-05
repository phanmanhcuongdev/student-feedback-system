package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.out.EnterpriseSurveyReport;
import com.ttcs.backend.application.port.out.LoadSurveyReportPort;
import com.ttcs.backend.application.port.out.OrganizationBranding;
import com.ttcs.backend.application.port.out.ReportFilterCriteria;
import com.ttcs.backend.application.port.out.ReportPeriod;
import com.ttcs.backend.application.port.out.RenderedReport;
import com.ttcs.backend.application.port.out.SummaryStatistics;
import com.ttcs.backend.application.port.out.SurveyReport;
import com.ttcs.backend.application.port.out.SurveyReportQuestion;
import com.ttcs.backend.application.port.out.SurveyReportRatingBreakdown;
import com.ttcs.backend.application.port.out.SurveyReportRenderer;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SurveyReportExportServiceTest {

    @Test
    void shouldLoadReportAndDelegateRenderingForAdmin() {
        TrackingSurveyReportRenderer renderer = new TrackingSurveyReportRenderer();
        SurveyReportExportService service = new SurveyReportExportService((surveyId, generatedByUserId) -> Optional.of(report(surveyId)), renderer);

        ExportedReport result = service.exportSurveyReport(12, 99, Role.ADMIN, "pdf");

        assertEquals("Course_Feedback_20260201.pdf", result.filename());
        assertEquals("application/pdf", result.contentType());
        assertEquals(1, renderer.renderCount);
        assertEquals(12, renderer.report.id());
        assertEquals("Course, Feedback", renderer.report.title());
        assertEquals(2, renderer.report.questions().size());
        assertEquals("pdf", renderer.format);
    }

    @Test
    void shouldRejectNonAdminExport() {
        TrackingSurveyReportPort port = new TrackingSurveyReportPort(Optional.of(report(12)));
        TrackingSurveyReportRenderer renderer = new TrackingSurveyReportRenderer();
        SurveyReportExportService service = new SurveyReportExportService(port, renderer);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.exportSurveyReport(12, 10, Role.LECTURER, "pdf")
        );

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("Only admins can export survey reports", exception.getReason());
        assertEquals(0, port.loadCount);
        assertEquals(0, renderer.renderCount);
    }

    @Test
    void shouldReturnNotFoundWhenSurveyReportDoesNotExist() {
        TrackingSurveyReportRenderer renderer = new TrackingSurveyReportRenderer();
        SurveyReportExportService service = new SurveyReportExportService((surveyId, generatedByUserId) -> Optional.empty(), renderer);

        assertThrows(SurveyNotFoundException.class, () -> service.exportSurveyReport(404, 99, Role.ADMIN, "pdf"));
        assertEquals(0, renderer.renderCount);
    }

    private static EnterpriseSurveyReport report(Integer surveyId) {
        return new EnterpriseSurveyReport(
                surveyId,
                "Course, Feedback",
                "Line one\nLine two",
                LocalDateTime.of(2026, 1, 1, 8, 30),
                LocalDateTime.of(2026, 1, 31, 17, 0),
                "PUBLISHED",
                "CLOSED",
                "DEPARTMENT",
                "Computer Science",
                30,
                20,
                12,
                40.0,
                List.of(
                        new SurveyReportQuestion(
                                100,
                                "How useful?",
                                "RATING",
                                2,
                                4.5,
                                List.of(
                                        new SurveyReportRatingBreakdown(5, 1),
                                        new SurveyReportRatingBreakdown(4, 1)
                                ),
                                List.of()
                        ),
                        new SurveyReportQuestion(
                                101,
                                "What changed?",
                                "TEXT",
                                1,
                                null,
                                List.of(),
                                List.of("Great, but needs \"examples\"")
                        )
                ),
                "admin@example.com",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                new OrganizationBranding("Student Feedback System", null, "#0f766e", "Internal report"),
                new ReportPeriod(LocalDateTime.of(2026, 1, 1, 8, 30), LocalDateTime.of(2026, 1, 31, 17, 0), "Survey active period"),
                new ReportFilterCriteria(String.valueOf(surveyId), "PUBLISHED", "CLOSED", "DEPARTMENT", "Computer Science"),
                new SummaryStatistics(30, 20, 12, 40.0, 2, 1, 1, 1, 4.5)
        );
    }

    private static final class TrackingSurveyReportPort implements LoadSurveyReportPort {
        private final Optional<EnterpriseSurveyReport> result;
        private int loadCount;

        private TrackingSurveyReportPort(Optional<EnterpriseSurveyReport> result) {
            this.result = result;
        }

        @Override
        public Optional<EnterpriseSurveyReport> loadSurveyReport(Integer surveyId, Integer generatedByUserId) {
            loadCount++;
            return result;
        }
    }

    private static final class TrackingSurveyReportRenderer implements SurveyReportRenderer {
        private int renderCount;
        private SurveyReport report;
        private String format;

        @Override
        public RenderedReport render(SurveyReport report, String format) {
            renderCount++;
            this.report = report;
            this.format = format;
            return new RenderedReport("Course_Feedback_20260201." + format, "application/pdf", new byte[]{1, 2, 3});
        }
    }
}
