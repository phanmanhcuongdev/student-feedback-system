package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FeedbackResponse {
    private final Integer id;
    private final Feedback feedback;
    private final User responder;
    private final String content;
    private final LocalDateTime createdAt;
}
