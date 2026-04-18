package com.ttcs.backend.application.port.in;

public record GetStudentSurveysQuery(
        String status,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
