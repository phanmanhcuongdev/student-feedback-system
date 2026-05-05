package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public class EnterpriseSurveyReport extends SurveyReport {

    private final String generatedBy;
    private final LocalDateTime generatedAt;
    private final OrganizationBranding organizationBranding;
    private final ReportPeriod reportPeriod;
    private final ReportFilterCriteria filterCriteria;
    private final SummaryStatistics summaryStatistics;

    public EnterpriseSurveyReport(
            Integer id,
            String title,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String lifecycleState,
            String runtimeStatus,
            String recipientScope,
            String recipientDepartmentName,
            long targetedCount,
            long openedCount,
            long submittedCount,
            double responseRate,
            List<SurveyReportQuestion> questions,
            String generatedBy,
            LocalDateTime generatedAt,
            OrganizationBranding organizationBranding,
            ReportPeriod reportPeriod,
            ReportFilterCriteria filterCriteria,
            SummaryStatistics summaryStatistics
    ) {
        super(id, title, description, startDate, endDate, lifecycleState, runtimeStatus, recipientScope,
                recipientDepartmentName, targetedCount, openedCount, submittedCount, responseRate, questions);
        this.generatedBy = generatedBy;
        this.generatedAt = generatedAt;
        this.organizationBranding = organizationBranding;
        this.reportPeriod = reportPeriod;
        this.filterCriteria = filterCriteria;
        this.summaryStatistics = summaryStatistics;
    }

    public String generatedBy() { return generatedBy; }
    public LocalDateTime generatedAt() { return generatedAt; }
    public OrganizationBranding organizationBranding() { return organizationBranding; }
    public ReportPeriod reportPeriod() { return reportPeriod; }
    public ReportFilterCriteria filterCriteria() { return filterCriteria; }
    public SummaryStatistics summaryStatistics() { return summaryStatistics; }

    public String getGeneratedBy() { return generatedBy; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public OrganizationBranding getOrganizationBranding() { return organizationBranding; }
    public ReportPeriod getReportPeriod() { return reportPeriod; }
    public ReportFilterCriteria getFilterCriteria() { return filterCriteria; }
    public SummaryStatistics getSummaryStatistics() { return summaryStatistics; }
}
