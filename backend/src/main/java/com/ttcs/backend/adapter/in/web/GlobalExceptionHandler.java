package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.ApiErrorResponse;
import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.exception.VerifyEmailDeliveryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SurveyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSurveyNotFound(SurveyNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("SURVEY_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(VerifyEmailDeliveryException.class)
    public ResponseEntity<ApiErrorResponse> handleVerifyEmailDelivery(VerifyEmailDeliveryException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiErrorResponse("EMAIL_DELIVERY_FAILED", exception.getMessage()));
    }
}
