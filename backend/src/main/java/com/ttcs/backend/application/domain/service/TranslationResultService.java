package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.port.in.translation.ApplyTranslationResultCommand;
import com.ttcs.backend.application.port.in.translation.ApplyTranslationResultUseCase;
import com.ttcs.backend.application.port.out.translation.TranslatedContentUpdateCommand;
import com.ttcs.backend.application.port.out.translation.UpdateTranslatedContentPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@UseCase
@RequiredArgsConstructor
public class TranslationResultService implements ApplyTranslationResultUseCase {

    private static final Logger log = LoggerFactory.getLogger(TranslationResultService.class);
    private static final Set<String> SUPPORTED_ENTITY_TYPES = Set.of("FEEDBACK", "QUESTION", "SURVEY_QUESTION");
    private static final String DEFAULT_MODEL_INFO = "default_model";

    private final UpdateTranslatedContentPort updateTranslatedContentPort;

    @Override
    @Transactional
    public boolean apply(ApplyTranslationResultCommand command) {
        if (command == null
                || command.entityId() == null
                || isBlank(command.entityType())
                || isBlank(command.translatedContent())
                || isBlank(command.sourceLang())
                || isBlank(command.targetLang())
                || !command.autoTranslated()) {
            log.warn("Reject invalid translation result: entityId={}, entityType={}, sourceLang={}, targetLang={}",
                    command == null ? null : command.entityId(),
                    command == null ? null : command.entityType(),
                    command == null ? null : command.sourceLang(),
                    command == null ? null : command.targetLang());
            return false;
        }

        String entityType = normalizeEntityType(command.entityType());
        if (!SUPPORTED_ENTITY_TYPES.contains(entityType)) {
            log.warn("Reject unsupported translation entity type: entityId={}, entityType={}", command.entityId(), command.entityType());
            return false;
        }

        boolean updated = updateTranslatedContentPort.updateTranslatedContent(new TranslatedContentUpdateCommand(
                command.entityId(),
                entityType,
                command.translatedContent().trim(),
                command.sourceLang().trim().toLowerCase(),
                command.targetLang().trim().toLowerCase(),
                normalizeModelInfo(command.modelInfo())
        ));
        if (updated) {
            log.info("Applied translation result: entityId={}, entityType={}, sourceLang={}, targetLang={}, modelInfo={}",
                    command.entityId(), entityType, command.sourceLang(), command.targetLang(), normalizeModelInfo(command.modelInfo()));
        } else {
            log.warn("Translation target not found: entityId={}, entityType={}", command.entityId(), entityType);
        }
        return updated;
    }

    private String normalizeEntityType(String value) {
        return value.trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeModelInfo(String value) {
        return isBlank(value) ? DEFAULT_MODEL_INFO : value.trim();
    }
}
