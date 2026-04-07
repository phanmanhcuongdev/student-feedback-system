package com.ttcs.backend.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StudentToken {
    private final Integer id;
    private final Student student;
    private final String token;
    private final LocalDateTime expiredAt;
    private final LocalDateTime createdAt;
    private final Integer deleteFlg;
}
