package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public class ReportPeriod {

    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String label;

    public ReportPeriod(LocalDateTime startDate, LocalDateTime endDate, String label) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.label = label;
    }

    public LocalDateTime startDate() { return startDate; }
    public LocalDateTime endDate() { return endDate; }
    public String label() { return label; }

    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public String getLabel() { return label; }
}
