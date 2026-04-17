package com.ttcs.backend.application.port.in.admin;

public record StudentDocumentResult(
        String filename,
        String contentType,
        byte[] content
) {
}
