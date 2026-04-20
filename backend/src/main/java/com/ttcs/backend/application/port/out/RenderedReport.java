package com.ttcs.backend.application.port.out;

public record RenderedReport(
        String filename,
        String contentType,
        byte[] content
) {
}
