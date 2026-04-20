package com.ttcs.backend.adapter.out.export;

import com.ttcs.backend.application.port.in.resultview.ExportedReport;
import com.ttcs.backend.application.port.in.resultview.SurveyReportQuestionView;
import com.ttcs.backend.application.port.in.resultview.SurveyReportRatingBreakdownView;
import com.ttcs.backend.application.port.in.resultview.SurveyReportView;
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

        ExportedReport result = renderer.render(report());

        String csv = new String(result.content(), StandardCharsets.UTF_8);
        assertEquals("survey-12-report.csv", result.filename());
        assertEquals("text/csv;charset=UTF-8", result.contentType());
        assertTrue(csv.contains("Survey ID,Survey Title,Description,Start Date,End Date,Lifecycle State,Runtime Status,Audience,Department"));
        assertTrue(csv.contains("12,\"Course, Feedback\",\"Line one"));
        assertTrue(csv.contains("How useful?,RATING,2,4.5,5,1,"));
        assertTrue(csv.contains("How useful?,RATING,2,4.5,4,1,"));
        assertTrue(csv.contains("What changed?,TEXT,1,,,," + "\"Great, but needs \"\"examples\"\"\""));
    }

    private SurveyReportView report() {
        return new SurveyReportView(
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
                        new SurveyReportQuestionView(
                                100,
                                "How useful?",
                                "RATING",
                                2,
                                4.5,
                                List.of(
                                        new SurveyReportRatingBreakdownView(5, 1),
                                        new SurveyReportRatingBreakdownView(4, 1)
                                ),
                                List.of()
                        ),
                        new SurveyReportQuestionView(
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
