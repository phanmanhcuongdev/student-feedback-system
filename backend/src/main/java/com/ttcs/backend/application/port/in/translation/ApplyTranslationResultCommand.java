package com.ttcs.backend.application.port.in.translation;

public record ApplyTranslationResultCommand(
        Integer entityId,
        String entityType,
        String translatedContentVi,
        String translatedContentEn,
        String sourceLang,
        String modelInfo,
        boolean autoTranslated
) {
}
