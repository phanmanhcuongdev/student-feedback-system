package com.ttcs.backend.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadDocumentsResponse {
    private boolean success;
    private String code;
    private String message;
}

