package com.ttcs.backend.application.port.in.admin;

public record ManagedUserMetricsResult(
        long totalUsers,
        long totalStudents,
        long totalTeachers,
        long totalAdmins,
        long totalInactive,
        long totalPending
) {
}
