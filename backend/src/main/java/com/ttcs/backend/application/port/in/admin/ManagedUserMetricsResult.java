package com.ttcs.backend.application.port.in.admin;

public record ManagedUserMetricsResult(
        long totalUsers,
        long totalStudents,
        long totalLecturers,
        long totalAdmins,
        long totalInactive,
        long totalPending
) {
}
