package com.ttcs.backend.adapter.out.export;

import com.ttcs.backend.application.port.out.RenderedReport;
import com.ttcs.backend.application.port.out.SurveyReport;
import com.ttcs.backend.application.port.out.SurveyReportQuestion;
import com.ttcs.backend.application.port.out.SurveyReportRatingBreakdown;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvSurveyReportRendererTest {

    @Test
    void shouldRenderCsvWithEscapedValues() {
        CsvSurveyReportRenderer renderer = new CsvSurveyReportRenderer();

        RenderedReport result = renderer.render(report());

        String csv = new String(result.content(), StandardCharsets.UTF_8);
        assertEquals("survey-12-report.csv", result.filename());
        assertEquals("text/csv;charset=UTF-8", result.contentType());
        assertTrue(csv.contains("Survey ID,Survey Title,Description,Start Date,End Date,Lifecycle State,Runtime Status,Audience,Department"));
        assertTrue(csv.contains("12,\"Course, Feedback\",\"Line one"));
        assertTrue(csv.contains("How useful?,RATING,2,4.5,5,1,"));
        assertTrue(csv.contains("How useful?,RATING,2,4.5,4,1,"));
        assertTrue(csv.contains("What changed?,TEXT,1,,,," + "\"Great, but needs \"\"examples\"\"\""));
    }

    private SurveyReport report() {
        return new SurveyReport(
                12,
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
}
