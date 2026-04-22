package com.ttcs.backend.application.port.out.ai;

public record TranslationTaskCommand(
        Integer entityId,
        String entityType,
        String content,
        String sourceLang,
        String targetLang
) {
    public TranslationTaskCommand(Integer entityId, String entityType, String content, String targetLang) {
        this(entityId, entityType, content, "auto", targetLang);
    }
}
