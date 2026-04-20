package com.ttcs.backend.application.port.in.admin;

public record GetSurveyTemplatesQuery(
        String keyword,
        Boolean active,
        int page,
        int size
) {
}
