package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public class SurveyReport {

    private final Integer id;
    private final String title;
    private final String description;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String lifecycleState;
    private final String runtimeStatus;
    private final String recipientScope;
    private final String recipientDepartmentName;
    private final long targetedCount;
    private final long openedCount;
    private final long submittedCount;
    private final double responseRate;
    private final List<SurveyReportQuestion> questions;

    public SurveyReport(
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
            List<SurveyReportQuestion> questions
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.lifecycleState = lifecycleState;
        this.runtimeStatus = runtimeStatus;
        this.recipientScope = recipientScope;
        this.recipientDepartmentName = recipientDepartmentName;
        this.targetedCount = targetedCount;
        this.openedCount = openedCount;
        this.submittedCount = submittedCount;
        this.responseRate = responseRate;
        this.questions = questions == null ? List.of() : List.copyOf(questions);
    }

    public Integer id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public LocalDateTime startDate() { return startDate; }
    public LocalDateTime endDate() { return endDate; }
    public String lifecycleState() { return lifecycleState; }
    public String runtimeStatus() { return runtimeStatus; }
    public String recipientScope() { return recipientScope; }
    public String recipientDepartmentName() { return recipientDepartmentName; }
    public long targetedCount() { return targetedCount; }
    public long openedCount() { return openedCount; }
    public long submittedCount() { return submittedCount; }
    public double responseRate() { return responseRate; }
    public List<SurveyReportQuestion> questions() { return questions; }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public String getLifecycleState() { return lifecycleState; }
    public String getRuntimeStatus() { return runtimeStatus; }
    public String getRecipientScope() { return recipientScope; }
    public String getRecipientDepartmentName() { return recipientDepartmentName; }
    public long getTargetedCount() { return targetedCount; }
    public long getOpenedCount() { return openedCount; }
    public long getSubmittedCount() { return submittedCount; }
    public double getResponseRate() { return responseRate; }
    public List<SurveyReportQuestion> getQuestions() { return questions; }
}
