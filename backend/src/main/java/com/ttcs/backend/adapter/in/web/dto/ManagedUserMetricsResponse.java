package com.ttcs.backend.adapter.in.web.dto;

public record ManagedUserMetricsResponse(
        long totalUsers,
        long totalStudents,
        long totalTeachers,
        long totalAdmins,
        long totalInactive,
        long totalPending
) {
}
