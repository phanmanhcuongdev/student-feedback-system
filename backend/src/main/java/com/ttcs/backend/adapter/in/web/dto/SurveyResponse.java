package com.ttcs.backend.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SurveyResponse {
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer createdBy;
    private String status;
    private boolean submitted;
}
