package com.ttcs.backend.adapter.out.export;

import com.ttcs.backend.application.port.out.EnterpriseSurveyReport;
import com.ttcs.backend.application.port.out.OrganizationBranding;
import com.ttcs.backend.application.port.out.RenderedReport;
import com.ttcs.backend.application.port.out.ReportFilterCriteria;
import com.ttcs.backend.application.port.out.ReportPeriod;
import com.ttcs.backend.application.port.out.SummaryStatistics;
import com.ttcs.backend.application.port.out.SurveyReportQuestion;
import com.ttcs.backend.application.port.out.SurveyReportRatingBreakdown;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BirtSurveyReportRendererSmokeTest {

    private final BirtEngineFactory engineFactory = new BirtEngineFactory();

    @AfterEach
    void tearDown() {
        engineFactory.shutdown();
    }

    @Test
    void renderConfiguredTemplateToPdfAndXlsx() throws IOException {
        BirtReportProperties properties = new BirtReportProperties();
        properties.setTemplatePath("classpath:/reports/survey_template.rptdesign");
        BirtSurveyReportRenderer renderer = new BirtSurveyReportRenderer(engineFactory, properties);
        EnterpriseSurveyReport report = sampleReport();

        RenderedReport pdf = renderer.render(report, "pdf");
        RenderedReport xlsx = renderer.render(report, "xlsx");

        assertEquals("application/pdf", pdf.contentType());
        assertTrue(pdf.content().length > 0);
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsx.contentType());
        assertTrue(xlsx.content().length > 0);
        String workbookText = workbookText(xlsx.content());
        assertTrue(workbookText.contains("30"), workbookText);
        assertTrue(workbookText.contains("18"), workbookText);
        assertTrue(workbookText.contains("Giang vien truyen dat noi dung ro rang va de hieu"), workbookText);
        assertTrue(workbookText.contains("Tai lieu hoc tap va bai tap phu hop voi muc tieu mon hoc"), workbookText);

        Path outputDir = Path.of("target", "birt-smoke");
        Files.createDirectories(outputDir);
        Files.write(outputDir.resolve(pdf.filename()), pdf.content());
        Files.write(outputDir.resolve(xlsx.filename()), xlsx.content());
    }

    private String workbookText(byte[] content) throws IOException {
        StringBuilder text = new StringBuilder();
        DataFormatter formatter = new DataFormatter();
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            workbook.forEach(sheet -> sheet.forEach(row -> row.forEach(cell ->
                    text.append(formatter.formatCellValue(cell)).append('\n')
            )));
        }
        return text.toString();
    }

    private EnterpriseSurveyReport sampleReport() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 28, 13, 30);
        List<SurveyReportQuestion> questions = List.of(
                new SurveyReportQuestion(
                        1,
                        "Giang vien truyen dat noi dung ro rang va de hieu",
                        "RATING",
                        18,
                        4.42,
                        List.of(
                                new SurveyReportRatingBreakdown(1, 0),
                                new SurveyReportRatingBreakdown(2, 1),
                                new SurveyReportRatingBreakdown(3, 2),
                                new SurveyReportRatingBreakdown(4, 6),
                                new SurveyReportRatingBreakdown(5, 9)
                        ),
                        List.of()
                ),
                new SurveyReportQuestion(
                        2,
                        "Tai lieu hoc tap va bai tap phu hop voi muc tieu mon hoc",
                        "RATING",
                        18,
                        3.86,
                        List.of(
                                new SurveyReportRatingBreakdown(1, 0),
                                new SurveyReportRatingBreakdown(2, 2),
                                new SurveyReportRatingBreakdown(3, 4),
                                new SurveyReportRatingBreakdown(4, 7),
                                new SurveyReportRatingBreakdown(5, 5)
                        ),
                        List.of()
                ),
                new SurveyReportQuestion(
                        3,
                        "Dong gop y kien them",
                        "TEXT",
                        6,
                        0.0,
                        List.of(),
                        List.of("Can them vi du thuc te trong buoi hoc.", "Nen cong bo rubric som hon.")
                )
        );

        return new EnterpriseSurveyReport(
                1000,
                "Enterprise Student Feedback",
                "Feedback summary for enterprise report smoke test",
                now.minusDays(14),
                now.minusDays(1),
                "PUBLISHED",
                "CLOSED",
                "DEPARTMENT",
                "Computer Science",
                30,
                24,
                18,
                60.0,
                questions,
                "admin@example.com",
                now,
                new OrganizationBranding("Student Feedback System", null, "#0D9488", "Internal report"),
                new ReportPeriod(now.minusDays(14), now.minusDays(1), "Survey active period"),
                new ReportFilterCriteria("1000", "PUBLISHED", "CLOSED", "DEPARTMENT", "Computer Science"),
                new SummaryStatistics(30, 24, 18, 60.0, 3, 2, 1, 2, 4.14)
        );
    }
}
