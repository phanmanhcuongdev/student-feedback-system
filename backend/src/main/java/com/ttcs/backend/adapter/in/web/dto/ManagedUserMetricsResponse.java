package com.ttcs.backend.adapter.in.web.dto;

public record ManagedUserMetricsResponse(
        long totalUsers,
        long totalStudents,
        long totalLecturers,
        long totalAdmins,
        long totalInactive,
        long totalPending
) {
}
