package com.ttcs.backend.application.port.in.admin;

public interface GetStudentDocumentUseCase {
    StudentDocumentResult getDocument(Integer studentId, String documentType);
}
