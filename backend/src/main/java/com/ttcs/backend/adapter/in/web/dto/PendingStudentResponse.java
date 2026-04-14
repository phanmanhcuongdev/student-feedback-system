package com.ttcs.backend.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingStudentResponse {
    private Integer id;
    private String name;
    private String email;
    private String studentCode;
    private String departmentName;
    private String status;
    private String studentCardImageUrl;
    private String nationalIdImageUrl;
    private String reviewReason;
    private String reviewNotes;
    private Integer resubmissionCount;
}
