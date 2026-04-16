package com.ttcs.backend.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentOnboardingStatusResponse {
    private boolean success;
    private String code;
    private String message;
    private String status;
    private String reviewReason;
    private String reviewNotes;
    private boolean hasUploadedDocuments;
    private boolean canUploadDocuments;
    private int resubmissionCount;
}
