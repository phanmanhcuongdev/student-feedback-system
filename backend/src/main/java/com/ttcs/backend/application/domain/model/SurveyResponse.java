package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SurveyResponse {
    private final Integer id;
    private final Student student;
    private final Lecturer lecturer;
    private final Survey survey;
    private final LocalDateTime submittedAt;
}