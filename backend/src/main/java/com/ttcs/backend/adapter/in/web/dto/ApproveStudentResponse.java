package com.ttcs.backend.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApproveStudentResponse {
    private boolean success;
    private String code;
    private String message;
}

