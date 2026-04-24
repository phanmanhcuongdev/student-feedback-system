package com.ttcs.backend.application.port.in;

public record GetStudentSurveysQuery(
        String status,
        Boolean submitted,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
