package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Notification {
    private final Integer id;
    private final String content;
}