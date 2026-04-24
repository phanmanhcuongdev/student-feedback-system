package com.ttcs.backend.application.port.out;

public record LoadStudentSurveysQuery(
        Integer studentId,
        String status,
        Boolean submitted,
        String targetLang,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
