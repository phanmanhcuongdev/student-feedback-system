package com.ttcs.backend.application.port.in.admin;

public record GetUsersQuery(
        String role,
        String keyword,
        Boolean active,
        String studentStatus,
        Integer departmentId,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
