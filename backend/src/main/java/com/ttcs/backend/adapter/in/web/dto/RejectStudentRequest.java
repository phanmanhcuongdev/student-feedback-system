package com.ttcs.backend.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectStudentRequest {
    private String reviewReason;
    private String reviewNotes;
}
