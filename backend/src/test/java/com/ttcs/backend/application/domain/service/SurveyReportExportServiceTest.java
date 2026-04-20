package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.out.LoadSurveyReportPort;
import com.ttcs.backend.application.port.out.RenderedReport;
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
        SurveyReportExportService service = new SurveyReportExportService(surveyId -> Optional.of(report(surveyId)), renderer);

        ExportedReport result = service.exportSurveyReport(12, 99, Role.ADMIN);

        assertEquals("survey-12-report.csv", result.filename());
        assertEquals("text/csv;charset=UTF-8", result.contentType());
        assertEquals(1, renderer.renderCount);
        assertEquals(12, renderer.report.id());
        assertEquals("Course, Feedback", renderer.report.title());
        assertEquals(2, renderer.report.questions().size());
    }

    @Test
    void shouldRejectNonAdminExport() {
        TrackingSurveyReportPort port = new TrackingSurveyReportPort(Optional.of(report(12)));
        TrackingSurveyReportRenderer renderer = new TrackingSurveyReportRenderer();
        SurveyReportExportService service = new SurveyReportExportService(port, renderer);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.exportSurveyReport(12, 10, Role.LECTURER)
        );

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("Only admins can export survey reports", exception.getReason());
        assertEquals(0, port.loadCount);
        assertEquals(0, renderer.renderCount);
    }

    @Test
    void shouldReturnNotFoundWhenSurveyReportDoesNotExist() {
        TrackingSurveyReportRenderer renderer = new TrackingSurveyReportRenderer();
        SurveyReportExportService service = new SurveyReportExportService(surveyId -> Optional.empty(), renderer);

        assertThrows(SurveyNotFoundException.class, () -> service.exportSurveyReport(404, 99, Role.ADMIN));
        assertEquals(0, renderer.renderCount);
    }

    private static SurveyReport report(Integer surveyId) {
        return new SurveyReport(
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
                )
        );
    }

    private static final class TrackingSurveyReportPort implements LoadSurveyReportPort {
        private final Optional<SurveyReport> result;
        private int loadCount;

        private TrackingSurveyReportPort(Optional<SurveyReport> result) {
            this.result = result;
        }

        @Override
        public Optional<SurveyReport> loadSurveyReport(Integer surveyId) {
            loadCount++;
            return result;
        }
    }

    private static final class TrackingSurveyReportRenderer implements SurveyReportRenderer {
        private int renderCount;
        private SurveyReport report;

        @Override
        public RenderedReport render(SurveyReport report) {
            renderCount++;
            this.report = report;
            return new RenderedReport("survey-" + report.id() + "-report.csv", "text/csv;charset=UTF-8", new byte[]{1, 2, 3});
        }
    }
}
