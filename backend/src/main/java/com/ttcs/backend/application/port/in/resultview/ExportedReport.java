package com.ttcs.backend.application.port.in.resultview;

public record ExportedReport(
        String filename,
        String contentType,
        byte[] content
) {
}
