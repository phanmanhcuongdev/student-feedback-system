package com.ttcs.backend.application.port.out.auth;

public record StudentDocumentContent(
        String filename,
        String contentType,
        byte[] content
) {
}
