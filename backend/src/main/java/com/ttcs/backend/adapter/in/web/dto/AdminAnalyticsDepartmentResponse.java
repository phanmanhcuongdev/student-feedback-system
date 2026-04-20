package com.ttcs.backend.adapter.in.web.dto;

public record AdminAnalyticsDepartmentResponse(
        Integer departmentId,
        String departmentName,
        long surveyCount,
        long targetedCount,
        long openedCount,
        long submittedCount,
        double responseRate
) {
}
