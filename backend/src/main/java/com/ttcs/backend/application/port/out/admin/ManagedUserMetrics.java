package com.ttcs.backend.application.port.out.admin;

public record ManagedUserMetrics(
        long totalUsers,
        long totalStudents,
        long totalLecturers,
        long totalAdmins,
        long totalInactive,
        long totalPending
) {
}
