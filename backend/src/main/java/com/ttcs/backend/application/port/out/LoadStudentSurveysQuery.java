package com.ttcs.backend.application.port.out;

public record LoadStudentSurveysQuery(
        Integer studentId,
        String status,
        Boolean submitted,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
