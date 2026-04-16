package com.ttcs.backend.application.port.out.admin;

public record ManagedUserMetrics(
        long totalUsers,
        long totalStudents,
        long totalTeachers,
        long totalAdmins,
        long totalInactive,
        long totalPending
) {
}
