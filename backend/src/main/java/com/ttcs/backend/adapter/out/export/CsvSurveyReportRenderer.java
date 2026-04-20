package com.ttcs.backend.adapter.out.export;

import com.ttcs.backend.application.port.out.RenderedReport;
import com.ttcs.backend.application.port.out.SurveyReport;
import com.ttcs.backend.application.port.out.SurveyReportQuestion;
import com.ttcs.backend.application.port.out.SurveyReportRenderer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class CsvSurveyReportRenderer implements SurveyReportRenderer {

    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";

    @Override
    public RenderedReport render(SurveyReport report) {
        byte[] content = buildCsv(report).getBytes(StandardCharsets.UTF_8);
        return new RenderedReport("survey-" + report.id() + "-report.csv", CSV_CONTENT_TYPE, content);
    }

    private String buildCsv(SurveyReport report) {
        StringBuilder csv = new StringBuilder();
        appendRow(csv,
                "Survey ID",
                "Survey Title",
                "Description",
                "Start Date",
                "End Date",
                "Lifecycle State",
                "Runtime Status",
                "Audience",
                "Department",
                "Targeted",
                "Opened",
                "Submitted",
                "Response Rate",
                "Question ID",
                "Question",
                "Question Type",
                "Question Response Count",
                "Average Rating",
                "Rating",
                "Rating Count",
                "Comment"
        );
        for (SurveyReportQuestion question : report.questions()) {
            if ("RATING".equalsIgnoreCase(question.type())) {
                if (question.ratingBreakdown().isEmpty()) {
                    appendQuestionRow(csv, report, question, null, null, null);
                } else {
                    question.ratingBreakdown().forEach(item -> appendQuestionRow(csv, report, question, item.rating(), item.count(), null));
                }
            } else if (question.comments().isEmpty()) {
                appendQuestionRow(csv, report, question, null, null, null);
            } else {
                question.comments().forEach(comment -> appendQuestionRow(csv, report, question, null, null, comment));
            }
        }
        return csv.toString();
    }

    private void appendQuestionRow(
            StringBuilder csv,
            SurveyReport report,
            SurveyReportQuestion question,
            Integer rating,
            Long ratingCount,
            String comment
    ) {
        appendRow(csv,
                report.id(),
                report.title(),
                report.description(),
                report.startDate(),
                report.endDate(),
                report.lifecycleState(),
                report.runtimeStatus(),
                report.recipientScope(),
                report.recipientDepartmentName(),
                report.targetedCount(),
                report.openedCount(),
                report.submittedCount(),
                report.responseRate(),
                question.id(),
                question.content(),
                question.type(),
                question.responseCount(),
                question.averageRating(),
                rating,
                ratingCount,
                comment
        );
    }

    private void appendRow(StringBuilder csv, Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escapeCsv(values[i]));
        }
        csv.append('\n');
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
