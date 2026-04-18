package com.ttcs.backend.application.port.out;

public record LoadStudentSurveysQuery(
        Integer studentId,
        String status,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
