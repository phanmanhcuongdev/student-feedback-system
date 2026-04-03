package com.ttcs.backend.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CurrentStudentProvider {

    private final Integer currentStudentId;

    public CurrentStudentProvider(@Value("${app.auth.mock-student-id}") Integer currentStudentId) {
        this.currentStudentId = currentStudentId;
    }

    public Integer currentStudentId() {
        return currentStudentId;
    }
}
