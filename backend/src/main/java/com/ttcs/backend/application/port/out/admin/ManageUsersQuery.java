package com.ttcs.backend.application.port.out.admin;

public record ManageUsersQuery(
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
