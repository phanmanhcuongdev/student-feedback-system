package com.ttcs.backend.application.port.out.translation;

public record TranslatedContentUpdateCommand(
        Integer entityId,
        String entityType,
        String translatedContent,
        String sourceLang,
        String targetLang,
        String modelInfo
) {
}
