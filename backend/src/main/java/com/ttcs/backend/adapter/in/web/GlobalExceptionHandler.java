package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.ApiErrorResponse;
import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SurveyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSurveyNotFound(SurveyNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(exception.getMessage()));
    }
}
