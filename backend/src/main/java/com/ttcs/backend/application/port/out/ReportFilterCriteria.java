package com.ttcs.backend.application.port.out;

public class ReportFilterCriteria {

    private final String surveyId;
    private final String lifecycleState;
    private final String runtimeStatus;
    private final String recipientScope;
    private final String departmentName;

    public ReportFilterCriteria(
            String surveyId,
            String lifecycleState,
            String runtimeStatus,
            String recipientScope,
            String departmentName
    ) {
        this.surveyId = surveyId;
        this.lifecycleState = lifecycleState;
        this.runtimeStatus = runtimeStatus;
        this.recipientScope = recipientScope;
        this.departmentName = departmentName;
    }

    public String surveyId() { return surveyId; }
    public String lifecycleState() { return lifecycleState; }
    public String runtimeStatus() { return runtimeStatus; }
    public String recipientScope() { return recipientScope; }
    public String departmentName() { return departmentName; }

    public String getSurveyId() { return surveyId; }
    public String getLifecycleState() { return lifecycleState; }
    public String getRuntimeStatus() { return runtimeStatus; }
    public String getRecipientScope() { return recipientScope; }
    public String getDepartmentName() { return departmentName; }
}
