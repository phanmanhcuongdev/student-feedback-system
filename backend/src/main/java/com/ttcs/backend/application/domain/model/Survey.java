package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Survey {

    private final Integer id;
    private final String title;
    private final String description;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer createdBy;

    public boolean isNotStarted() {
        LocalDateTime now = LocalDateTime.now();
        return startDate != null && now.isBefore(startDate);
    }

    public boolean isClosed() {
        LocalDateTime now = LocalDateTime.now();
        return endDate != null && now.isAfter(endDate);
    }

    public boolean isOpen() {
        LocalDateTime now = LocalDateTime.now();

        boolean started = (startDate == null) || !now.isBefore(startDate);
        boolean notEnded = (endDate == null) || !now.isAfter(endDate);

        return started && notEnded;
    }
}