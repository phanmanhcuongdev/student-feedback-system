package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.ApiErrorResponse;
import com.ttcs.backend.application.domain.exception.FileTooLargeException;
import com.ttcs.backend.application.domain.exception.SurveyNotFoundException;
import com.ttcs.backend.application.domain.exception.VerifyEmailDeliveryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ApiErrorResponse> handleFileTooLarge(FileTooLargeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("FILE_TOO_LARGE", exception.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("FILE_TOO_LARGE", "Each uploaded file must not exceed 5MB."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        if ("USER_NOT_FOUND".equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse("USER_NOT_FOUND", "User was not found."));
        }
        if ("SURVEY_NOT_FOUND".equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse("SURVEY_NOT_FOUND", "Survey was not found."));
        }
        if ("STUDENT_NOT_FOUND".equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse("STUDENT_NOT_FOUND", "Student was not found."));
        }
        if ("DOCUMENT_NOT_FOUND".equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse("DOCUMENT_NOT_FOUND", "Document was not found."));
        }
        if ("INVALID_DOCUMENT_TYPE".equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse("INVALID_DOCUMENT_TYPE", "Document type is invalid."));
        }
        if ("INVALID_REQUEST".equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse("INVALID_REQUEST", "Request is invalid."));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("INVALID_REQUEST", exception.getMessage()));
    }
}
