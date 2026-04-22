package com.ttcs.backend.application.port.in.translation;

public record ApplyTranslationResultCommand(
        Integer entityId,
        String entityType,
        String translatedContent,
        String sourceLang,
        String targetLang,
        String modelInfo,
        boolean autoTranslated
) {
}
