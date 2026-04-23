package com.ttcs.backend.application.port.out.translation;

public record TranslatedContentUpdateCommand(
        Integer entityId,
        String entityType,
        String translatedContentVi,
        String translatedContentEn,
        String sourceLang,
        String modelInfo
) {
}
