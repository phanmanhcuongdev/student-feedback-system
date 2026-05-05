package com.ttcs.backend.adapter.out.export;

import com.ttcs.backend.application.port.out.EnterpriseSurveyReport;
import com.ttcs.backend.application.port.out.RenderedReport;
import com.ttcs.backend.application.port.out.SurveyReport;
import com.ttcs.backend.application.port.out.SurveyReportRenderer;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class BirtSurveyReportRenderer implements SurveyReportRenderer {

    public static final String APP_CONTEXT_REPORTS = "enterpriseSurveyReports";
    public static final String APP_CONTEXT_REPORT = "enterpriseSurveyReport";
    public static final String APP_CONTEXT_ROWS = "rows";
    public static final String APP_CONTEXT_SUMMARY = "summary";
    public static final String APP_CONTEXT_BRANDING = "branding";
    public static final String APP_CONTEXT_FILTER_CRITERIA = "filterCriteria";
    public static final String APP_CONTEXT_REPORT_PERIOD = "reportPeriod";

    private static final Logger log = LoggerFactory.getLogger(BirtSurveyReportRenderer.class);

    private final BirtEngineFactory engineFactory;
    private final BirtReportProperties properties;

    public BirtSurveyReportRenderer(
            BirtEngineFactory engineFactory,
            BirtReportProperties properties
    ) {
        this.engineFactory = engineFactory;
        this.properties = properties;
    }

    @Override
    public RenderedReport render(SurveyReport report, String requestedFormat) {
        if (!(report instanceof EnterpriseSurveyReport enterpriseReport)) {
            throw new BirtRenderException("BIRT renderer requires EnterpriseSurveyReport input", null);
        }

        String format = normalizeFormat(requestedFormat);
        long startedAt = System.nanoTime();
        log.info("Starting BIRT render for surveyId={} format={}", enterpriseReport.id(), format);
        try {
            byte[] content = renderReport(enterpriseReport, format);
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.debug("BIRT render completed for surveyId={} format={} in {} ms", enterpriseReport.id(), format, elapsedMs);
            return new RenderedReport(filename(enterpriseReport, format), contentType(format), content);
        } catch (BirtRenderException exception) {
            throw exception;
        } catch (Throwable exception) {
            log.error("Failed to render survey report {} with BIRT", enterpriseReport.id(), exception);
            throw new BirtRenderException("Unable to render survey report. Please try again later.", exception);
        }
    }

    private byte[] renderReport(EnterpriseSurveyReport report, String format) throws Exception {
        ClassPathResource template = new ClassPathResource(normalizeClasspathLocation(properties.getTemplatePath()));
        if (!template.exists()) {
            throw new BirtRenderException("BIRT report template was not found: " + properties.getTemplatePath(), null);
        }

        IReportEngine engine = engineFactory.getEngine();
        try (InputStream templateStream = patchedTemplateStream(template, report);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            IReportRunnable design = engine.openReportDesign(template.getFilename(), templateStream);
            IRunAndRenderTask task = engine.createRunAndRenderTask(design);
            try {
                task.setLocale(Locale.getDefault());
                loadPojoData(task, report);

                RenderOption options = renderOptions(format);
                options.setOutputStream(output);
                task.setRenderOption(options);
                task.run();
                if (!task.getErrors().isEmpty()) {
                    log.warn("BIRT completed surveyId={} format={} with {} engine warnings/errors: {}",
                            report.id(), format, task.getErrors().size(), task.getErrors());
                }
                return output.toByteArray();
            } finally {
                task.close();
            }
        }
    }

    private InputStream patchedTemplateStream(ClassPathResource template, EnterpriseSurveyReport report) throws Exception {
        String designXml;
        try (InputStream inputStream = template.getInputStream()) {
            designXml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        designXml = designXml
                .replace(
                        "summary = reportContext.getAppContext().get(\"summary\");\r\n                fetched = false;",
                        "this.summary = reportContext.getAppContext().get(\"summary\");\r\n"
                                + "                if (this.summary == null) this.summary = reportContext.getAppContext().get(\"summaryStatistics\");\r\n"
                                + "                this.fetched = false;"
                )
                .replace(
                        "summary = reportContext.getAppContext().get(\"summary\");\n                fetched = false;",
                        "this.summary = reportContext.getAppContext().get(\"summary\");\n"
                                + "                if (this.summary == null) this.summary = reportContext.getAppContext().get(\"summaryStatistics\");\n"
                                + "                this.fetched = false;"
                )
                .replace("if (fetched || summary == null) return false;", "if (this.fetched || this.summary == null) return false;")
                .replace("summary.getTargetedCount()", "this.summary.getTargetedCount()")
                .replace("summary.getSubmittedCount()", "this.summary.getSubmittedCount()")
                .replace("summary.getResponseRate()", "this.summary.getResponseRate()")
                .replace("summary.getAverageRating()", "this.summary.getAverageRating()")
                .replace("fetched = true;", "this.fetched = true;")
                .replace(
                        "qs = reportContext.getAppContext().get(\"rows\");\r\n                idx = 0;",
                        "this.qs = reportContext.getAppContext().get(\"rows\");\r\n"
                                + "                if (this.qs == null) this.qs = reportContext.getAppContext().get(\"questions\");\r\n"
                                + "                this.idx = 0;"
                )
                .replace(
                        "qs = reportContext.getAppContext().get(\"rows\");\n                idx = 0;",
                        "this.qs = reportContext.getAppContext().get(\"rows\");\n"
                                + "                if (this.qs == null) this.qs = reportContext.getAppContext().get(\"questions\");\n"
                                + "                this.idx = 0;"
                )
                .replace("if (qs == null || idx >= qs.size()) return false;", "if (this.qs == null || this.idx >= this.qs.size()) return false;")
                .replace("var q = qs.get(idx++);", "var q = this.qs.get(this.idx++);");

        designXml = enforceRobotoStyles(designXml);
        designXml = hydrateTemplateData(designXml, report);

        return new java.io.ByteArrayInputStream(designXml.getBytes(StandardCharsets.UTF_8));
    }

    private String hydrateTemplateData(String designXml, EnterpriseSurveyReport report) {
        String hydrated = replaceFirst(
                replaceFirst(
                        replaceFirst(designXml, "<list id=\"31\">.*?</list>", kpiGrid(report)),
                        "<table id=\"51\">.*?</table>",
                        questionGrid(report)
                ),
                "<extended-item extensionName=\"Chart\" id=\"71\">.*?</extended-item>",
                ratingBarChart(report)
        );
        hydrated = replaceFirst(hydrated, "<label id=\"30\">.*?</label>", brandingHeader(report));
        hydrated = replaceFirst(hydrated, "<page-footer>.*?</page-footer>", metadataFooter(report));
        return hydrated;
    }

    private String enforceRobotoStyles(String designXml) {
        return designXml
                .replace(
                        "<style name=\"TableHeaderStyle\" id=\"5\">",
                        "<style name=\"TableHeaderStyle\" id=\"5\">\n            <property name=\"fontFamily\">Roboto, Arial, sans-serif</property>"
                )
                .replace(
                        "<style name=\"TableCellStyle\" id=\"6\">",
                        "<style name=\"TableCellStyle\" id=\"6\">\n            <property name=\"fontFamily\">Roboto, Arial, sans-serif</property>"
                )
                .replace(
                        "<style name=\"KpiCardStyle\" id=\"4\">",
                        "<style name=\"KpiCardStyle\" id=\"4\">\n            <property name=\"fontFamily\">Roboto, Arial, sans-serif</property>"
                );
    }

    private String replaceFirst(String value, String regex, String replacement) {
        return java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL)
                .matcher(value)
                .replaceFirst(java.util.regex.Matcher.quoteReplacement(replacement));
    }

    private String kpiGrid(EnterpriseSurveyReport report) {
        var summary = report.summaryStatistics();
        long targeted = summary == null ? 0 : summary.targetedCount();
        long opened = summary == null ? 0 : summary.openedCount();
        long submitted = summary == null ? 0 : summary.submittedCount();
        double openedRate = targeted <= 0 ? 0.0 : (opened * 100.0 / targeted);
        double submittedRate = targeted <= 0 ? 0.0 : (submitted * 100.0 / targeted);
        int openedWidth = percentWidth(openedRate);
        int submittedWidth = percentWidth(submittedRate);
        int openedSpacer = 100 - openedWidth;
        int submittedSpacer = 100 - submittedWidth;
        return """
        <grid id="9001">
            <property name="width">100%%</property>
            <property name="paddingTop">4pt</property>
            <property name="paddingBottom">8pt</property>
            <column id="9002"><property name="width">100%%</property></column>
            <row id="9006">
                <cell id="9007">
                    <grid id="9008">
                        <property name="width">100%%</property>
                        <column id="9009"><property name="width">100%%</property></column>
                        <row id="9010">
                            <cell id="9011">
                                <property name="backgroundColor">#0F766E</property>
                                <property name="paddingTop">10pt</property>
                                <property name="paddingBottom">10pt</property>
                                <property name="textAlign">center</property>
                                <label id="9012"><property name="fontSize">11pt</property><property name="fontWeight">bold</property><property name="color">#FFFFFF</property><text-property name="text">%s</text-property></label>
                            </cell>
                        </row>
                    </grid>
                </cell>
            </row>
            <row id="9013">
                <cell id="9014">
                    <grid id="9015">
                        <property name="width">100%%</property>
                        <column id="9016"><property name="width">%d%%</property></column>
                        <column id="9017"><property name="width">%d%%</property></column>
                        <row id="9018">
                            <cell id="9019">
                                <property name="backgroundColor">#0D9488</property>
                                <property name="paddingTop">9pt</property>
                                <property name="paddingBottom">9pt</property>
                                <property name="textAlign">center</property>
                                <label id="9020"><property name="fontSize">10pt</property><property name="fontWeight">bold</property><property name="color">#FFFFFF</property><text-property name="text">%s</text-property></label>
                            </cell>
                            <cell id="9021"><label id="9022"><property name="fontSize">1pt</property><text-property name="text"> </text-property></label></cell>
                        </row>
                    </grid>
                </cell>
            </row>
            <row id="9023">
                <cell id="9024">
                    <grid id="9025">
                        <property name="width">100%%</property>
                        <column id="9026"><property name="width">%d%%</property></column>
                        <column id="9027"><property name="width">%d%%</property></column>
                        <row id="9028">
                            <cell id="9029">
                                <property name="backgroundColor">#5EEAD4</property>
                                <property name="paddingTop">9pt</property>
                                <property name="paddingBottom">9pt</property>
                                <property name="textAlign">center</property>
                                <label id="9030"><property name="fontSize">10pt</property><property name="fontWeight">bold</property><property name="color">#0F172A</property><text-property name="text">%s</text-property></label>
                            </cell>
                            <cell id="9031"><label id="9032"><property name="fontSize">1pt</property><text-property name="text"> </text-property></label></cell>
                        </row>
                    </grid>
                </cell>
            </row>
        </grid>
        """.formatted(
                xml("Targeted: " + targeted + " (100%)"),
                openedWidth,
                openedSpacer,
                xml("Opened: " + opened + " (" + String.format(Locale.US, "%.1f%%", openedRate) + ")"),
                submittedWidth,
                submittedSpacer,
                xml("Submitted: " + submitted + " (" + String.format(Locale.US, "%.1f%%", submittedRate) + ")")
        );
    }

    private int percentWidth(double value) {
        if (value <= 0.0) {
            return 1;
        }
        return Math.max(1, Math.min(100, (int) Math.round(value)));
    }

    private String brandingHeader(EnterpriseSurveyReport report) {
        var branding = report.organizationBranding();
        String organizationName = branding == null || isBlank(branding.organizationName())
                ? "Student Feedback System"
                : branding.organizationName();
        String logo = branding == null ? null : branding.logoUrl();
        return """
        <grid id="9600">
            <property name="width">100%%</property>
            <property name="paddingBottom">12pt</property>
            <column id="9601"><property name="width">28%%</property></column>
            <column id="9602"><property name="width">72%%</property></column>
            <row id="9603">
                <cell id="9604">%s</cell>
                <cell id="9607">
                    <property name="textAlign">right</property>
                    <label id="9608"><property name="fontFamily">Roboto, Arial, sans-serif</property><property name="fontSize">12pt</property><property name="fontWeight">bold</property><property name="color">#0F172A</property><text-property name="text">%s</text-property></label>
                    <label id="9609"><property name="fontFamily">Roboto, Arial, sans-serif</property><property name="fontSize">18pt</property><property name="fontWeight">bold</property><property name="color">#0D9488</property><text-property name="text">BÁO CÁO HỆ THỐNG PHẢN HỒI</text-property></label>
                </cell>
            </row>
        </grid>
        <label id="9610"><property name="style">ReportTitle</property><text-property name="text">Executive Dashboard</text-property></label>
        """.formatted(logoElement(logo), xml(organizationName));
    }

    private String logoElement(String logoUrl) {
        if (isBlank(logoUrl)) {
            return """
            <label id="9605"><property name="fontFamily">Roboto, Arial, sans-serif</property><property name="fontSize">11pt</property><property name="fontWeight">bold</property><property name="color">#0D9488</property><text-property name="text">Student Feedback</text-property></label>
            """;
        }
        return """
        <image id="9605">
            <property name="source">url</property>
            <property name="height">0.55in</property>
            <property name="width">1.65in</property>
            <expression name="uri" type="constant">%s</expression>
        </image>
        """.formatted(xml(logoUrl));
    }

    private String metadataFooter(EnterpriseSurveyReport report) {
        var branding = report.organizationBranding();
        String confidentialityLabel = branding == null || isBlank(branding.confidentialityLabel())
                ? "Lưu hành nội bộ"
                : branding.confidentialityLabel();
        String generatedAt = formatGeneratedAt(report.generatedAt());
        return """
        <page-footer>
            <grid id="9700">
                <property name="width">100%%</property>
                <property name="borderTopColor">#E2E8F0</property>
                <property name="borderTopStyle">solid</property>
                <property name="borderTopWidth">1pt</property>
                <property name="paddingTop">4pt</property>
                <column id="9701"><property name="width">40%%</property></column>
                <column id="9702"><property name="width">35%%</property></column>
                <column id="9703"><property name="width">25%%</property></column>
                <row id="9704">
                    <cell id="9705"><label id="9706"><property name="fontFamily">Roboto, Arial, sans-serif</property><property name="fontSize">8pt</property><property name="color">#64748B</property><text-property name="text">%s</text-property></label></cell>
                    <cell id="9707"><label id="9708"><property name="fontFamily">Roboto, Arial, sans-serif</property><property name="fontSize">8pt</property><property name="color">#64748B</property><text-property name="text">Generated at: %s</text-property></label></cell>
                    <cell id="9709"><property name="textAlign">right</property><data id="9710"><property name="fontFamily">Roboto, Arial, sans-serif</property><property name="fontSize">8pt</property><property name="color">#64748B</property><expression name="valueExpr">"Page " + pageNumber + " / " + totalPage</expression></data></cell>
                </row>
            </grid>
        </page-footer>
        """.formatted(xml(confidentialityLabel), xml(generatedAt));
    }

    private String formatGeneratedAt(LocalDateTime generatedAt) {
        LocalDateTime value = generatedAt == null ? LocalDateTime.now() : generatedAt;
        return value.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
    }

    private String questionGrid(EnterpriseSurveyReport report) {
        StringBuilder rows = new StringBuilder();
        int id = 9200;
        for (var question : report.questions()) {
            rows.append("""
            <row id="%d">
                <cell id="%d"><property name="textAlign">right</property><label id="%d"><property name="style">TableCellStyle</property><text-property name="text">%s</text-property></label></cell>
                <cell id="%d"><label id="%d"><property name="style">TableCellStyle</property><text-property name="text">%s</text-property></label></cell>
                <cell id="%d"><property name="textAlign">right</property><label id="%d"><property name="style">TableCellStyle</property><text-property name="text">%s</text-property></label></cell>
                <cell id="%d"><property name="textAlign">right</property><label id="%d"><property name="style">TableCellStyle</property><text-property name="text">%s</text-property></label></cell>
            </row>
            """.formatted(
                    id++,
                    id++, id++, xml(question.id()),
                    id++, id++, xml(question.content()),
                    id++, id++, xml(question.responseCount()),
                    id++, id++, question.averageRating() == null ? "-" : String.format(Locale.US, "%.2f", question.averageRating())
            ));
        }
        return """
        <grid id="9101">
            <property name="width">100%%</property>
            <column id="9102"><property name="width">8%%</property></column>
            <column id="9103"><property name="width">62%%</property></column>
            <column id="9104"><property name="width">15%%</property></column>
            <column id="9105"><property name="width">15%%</property></column>
            <row id="9105">
                <cell id="9106"><property name="textAlign">right</property><label id="9107"><property name="style">TableHeaderStyle</property><text-property name="text">ID</text-property></label></cell>
                <cell id="9108"><label id="9109"><property name="style">TableHeaderStyle</property><text-property name="text">Question</text-property></label></cell>
                <cell id="9110"><property name="textAlign">right</property><label id="9111"><property name="style">TableHeaderStyle</property><text-property name="text">Responses</text-property></label></cell>
                <cell id="9112"><property name="textAlign">right</property><label id="9113"><property name="style">TableHeaderStyle</property><text-property name="text">Avg Rating</text-property></label></cell>
            </row>
        %s
        </grid>
        """.formatted(rows);
    }

    private String ratingBarChart(EnterpriseSurveyReport report) {
        List<com.ttcs.backend.application.port.out.SurveyReportQuestion> ratingQuestions = report.questions().stream()
                .filter(question -> question.averageRating() != null && question.averageRating() > 0.0)
                .sorted(Comparator.comparing(
                        com.ttcs.backend.application.port.out.SurveyReportQuestion::averageRating,
                        Comparator.reverseOrder()
                ))
                .limit(5)
                .toList();

        StringBuilder rows = new StringBuilder();
        int id = 9400;
        if (ratingQuestions.isEmpty()) {
            rows.append("""
            <row id="%d">
                <cell id="%d"><label id="%d"><property name="style">TableCellStyle</property><text-property name="text">No rating data</text-property></label></cell>
                <cell id="%d"><label id="%d"><property name="style">TableCellStyle</property><text-property name="text">-</text-property></label></cell>
                <cell id="%d"><label id="%d"><property name="style">TableCellStyle</property><text-property name="text">-</text-property></label></cell>
            </row>
            """.formatted(id++, id++, id++, id++, id++, id++, id++));
        }

        for (var question : ratingQuestions) {
            double rating = Math.max(0.0, Math.min(5.0, question.averageRating()));
            int filledPercent = Math.max(1, (int) Math.round((rating / 5.0) * 100.0));
            int emptyPercent = Math.max(0, 100 - filledPercent);
            rows.append("""
            <row id="%d">
                <cell id="%d"><label id="%d"><property name="style">TableCellStyle</property><text-property name="text">%s</text-property></label></cell>
                <cell id="%d">
                    <grid id="%d">
                        <property name="width">100%%</property>
                        <column id="%d"><property name="width">%d%%</property></column>
                        <column id="%d"><property name="width">%d%%</property></column>
                        <row id="%d">
                            <cell id="%d"><property name="backgroundColor">#0D9488</property><label id="%d"><property name="fontSize">8pt</property><property name="color">#FFFFFF</property><text-property name="text">%s</text-property></label></cell>
                            <cell id="%d"><property name="backgroundColor">#E2E8F0</property><label id="%d"><property name="fontSize">8pt</property><text-property name="text"> </text-property></label></cell>
                        </row>
                    </grid>
                </cell>
                <cell id="%d"><property name="textAlign">right</property><label id="%d"><property name="style">TableCellStyle</property><text-property name="text">%s</text-property></label></cell>
            </row>
            """.formatted(
                    id++,
                    id++, id++, xml(shorten(question.content(), 72)),
                    id++,
                    id++,
                    id++, filledPercent,
                    id++, emptyPercent,
                    id++,
                    id++, id++, xml(String.format(Locale.US, "%.2f", rating)),
                    id++, id++,
                    id++, id++, xml(String.format(Locale.US, "%.2f", rating))
            ));
        }

        return """
        <grid id="9301">
            <property name="width">100%%</property>
            <property name="height">3in</property>
            <column id="9302"><property name="width">55%%</property></column>
            <column id="9303"><property name="width">35%%</property></column>
            <column id="9304"><property name="width">10%%</property></column>
            <row id="9305">
                <cell id="9306"><label id="9307"><property name="style">TableHeaderStyle</property><text-property name="text">Question</text-property></label></cell>
                <cell id="9308"><label id="9309"><property name="style">TableHeaderStyle</property><text-property name="text">Rating Bar</text-property></label></cell>
                <cell id="9310"><label id="9311"><property name="style">TableHeaderStyle</property><text-property name="text">Avg</text-property></label></cell>
            </row>
        %s
        </grid>
        """.formatted(rows);
    }

    private String shorten(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String xml(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeClasspathLocation(String location) {
        String value = location == null || location.isBlank()
                ? "reports/survey_template.rptdesign"
                : location.trim();
        if (value.startsWith("classpath:")) {
            value = value.substring("classpath:".length());
        }
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return value;
    }

    private RenderOption renderOptions(String format) {
        if ("pdf".equals(format)) {
            PDFRenderOption options = new PDFRenderOption();
            options.setOutputFormat("pdf");
            options.setEmbededFont(true);
            return options;
        }

        EXCELRenderOption options = new EXCELRenderOption();
        options.setOutputFormat("xlsx");
        options.setOfficeVersion("office2007");
        options.setEnableMultipleSheet(false);
        options.setHideGridlines(false);
        options.setWrappingText(true);
        return options;
    }

    private void loadPojoData(IRunAndRenderTask task, EnterpriseSurveyReport report) {
        Map<String, Object> appContext = task.getAppContext() == null ? new HashMap<>() : task.getAppContext();
        appContext.put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, BirtSurveyReportRenderer.class.getClassLoader());
        appContext.put(APP_CONTEXT_REPORTS, List.of(report));
        appContext.put(APP_CONTEXT_REPORT, report);
        appContext.put(APP_CONTEXT_ROWS, report.questions());
        appContext.put(APP_CONTEXT_SUMMARY, report.summaryStatistics());
        appContext.put(APP_CONTEXT_BRANDING, report.organizationBranding());
        appContext.put(APP_CONTEXT_FILTER_CRITERIA, report.filterCriteria());
        appContext.put(APP_CONTEXT_REPORT_PERIOD, report.reportPeriod());
        appContext.put("report", report);
        appContext.put("surveyReport", report);
        appContext.put("enterpriseReport", report);
        appContext.put("questions", report.questions());
        appContext.put("questionRows", report.questions());
        appContext.put("summaryStatistics", report.summaryStatistics());
        appContext.put("organizationBranding", report.organizationBranding());
        appContext.put("brand", report.organizationBranding());
        appContext.put("criteria", report.filterCriteria());
        appContext.put("period", report.reportPeriod());

        if (report.summaryStatistics() != null) {
            appContext.put("targetedCount", report.summaryStatistics().targetedCount());
            appContext.put("openedCount", report.summaryStatistics().openedCount());
            appContext.put("submittedCount", report.summaryStatistics().submittedCount());
            appContext.put("responseRate", report.summaryStatistics().responseRate());
            appContext.put("averageRating", report.summaryStatistics().averageRating());
            appContext.put("totalQuestions", report.summaryStatistics().totalQuestions());
            appContext.put("ratingQuestionCount", report.summaryStatistics().ratingQuestionCount());
            appContext.put("textQuestionCount", report.summaryStatistics().textQuestionCount());
            appContext.put("commentCount", report.summaryStatistics().commentCount());
        }

        appContext.put("generatedBy", report.generatedBy());
        appContext.put("generatedAt", report.generatedAt());
        task.setAppContext(appContext);
    }

    private String normalizeFormat(String value) {
        String fallbackFormat = properties.getOutputFormat() == null ? "pdf" : properties.getOutputFormat();
        String format = value == null || value.isBlank() ? fallbackFormat.trim().toLowerCase(Locale.ROOT) : value.trim().toLowerCase(Locale.ROOT);
        if (!"pdf".equals(format) && !"xlsx".equals(format)) {
            throw new BirtRenderException("Unsupported BIRT report format: " + value, null);
        }
        return format;
    }

    private String filename(EnterpriseSurveyReport report, String format) {
        LocalDate reportDate = report.generatedAt() == null ? LocalDate.now() : report.generatedAt().toLocalDate();
        return sanitizeFilename(report.title()) + "_" + reportDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "." + format;
    }

    private String sanitizeFilename(String value) {
        String normalized = value == null || value.isBlank() ? "survey_report" : value.trim();
        normalized = normalized.replaceAll("[^a-zA-Z0-9]+", "_");
        normalized = normalized.replaceAll("_+", "_");
        normalized = normalized.replaceAll("^_|_$", "");
        return normalized.isBlank() ? "survey_report" : normalized;
    }

    private String contentType(String format) {
        return switch (format) {
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }
}
