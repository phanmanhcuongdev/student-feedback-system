package com.ttcs.backend.application.port.out.admin;

public record ManagePendingStudentsQuery(
        String keyword,
        Integer departmentId,
        String submissionType,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
