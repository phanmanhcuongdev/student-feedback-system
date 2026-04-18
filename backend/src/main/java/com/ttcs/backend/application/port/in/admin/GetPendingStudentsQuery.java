package com.ttcs.backend.application.port.in.admin;

public record GetPendingStudentsQuery(
        String keyword,
        Integer departmentId,
        String submissionType,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
