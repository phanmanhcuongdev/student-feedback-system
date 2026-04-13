package com.ttcs.backend.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFeedbackRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
