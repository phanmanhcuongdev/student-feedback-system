package com.ttcs.backend.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ttcs.backend.application.port.in.translation.ApplyTranslationResultCommand;
import com.ttcs.backend.application.port.in.translation.ApplyTranslationResultUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TranslationReplyListener {

    private static final Logger log = LoggerFactory.getLogger(TranslationReplyListener.class);

    private final ApplyTranslationResultUseCase applyTranslationResultUseCase;

    @RabbitListener(queues = "${app.rabbitmq.reply-queue}")
    public void onMessage(TranslationReplyMessage message) {
        if (message == null) {
            log.warn("Received null translation reply message");
            throw new AmqpRejectAndDontRequeueException("Translation reply message must not be null");
        }

        try {
            boolean applied = applyTranslationResultUseCase.apply(new ApplyTranslationResultCommand(
                    message.entityId(),
                    message.entityType(),
                    message.translatedContentVi(),
                    message.translatedContentEn(),
                    message.sourceLang(),
                    message.modelInfo(),
                    message.autoTranslated()
            ));
            if (!applied) {
                String errorMessage = "Translation reply was rejected by application service";
                log.error("{}: entityId={}, entityType={}, sourceLang={}",
                        errorMessage, message.entityId(), message.entityType(), message.sourceLang());
                throw new AmqpRejectAndDontRequeueException(errorMessage);
            }
        } catch (RuntimeException exception) {
            log.error("Failed to process translation reply: entityId={}, entityType={}, sourceLang={}",
                    message.entityId(), message.entityType(), message.sourceLang(), exception);
            if (exception instanceof AmqpRejectAndDontRequeueException) {
                throw exception;
            }
            throw exception;
        }
    }

    public record TranslationReplyMessage(
            @JsonProperty("entity_id") Integer entityId,
            @JsonProperty("entity_type") String entityType,
            @JsonProperty("translated_content_vi") String translatedContentVi,
            @JsonProperty("translated_content_en") String translatedContentEn,
            @JsonProperty("source_lang") String sourceLang,
            @JsonProperty("model_info") String modelInfo,
            @JsonProperty("is_auto_translated") boolean autoTranslated,
            @JsonProperty("translated_content") String legacyTranslatedContent,
            @JsonProperty("target_lang") String legacyTargetLang
    ) {
        public String translatedContentVi() {
            if (translatedContentVi != null && !translatedContentVi.isBlank()) {
                return translatedContentVi;
            }
            return "vi".equals(normalizeLanguage(legacyTargetLang)) ? legacyTranslatedContent : null;
        }

        public String translatedContentEn() {
            if (translatedContentEn != null && !translatedContentEn.isBlank()) {
                return translatedContentEn;
            }
            return "en".equals(normalizeLanguage(legacyTargetLang)) ? legacyTranslatedContent : null;
        }

        private String normalizeLanguage(String value) {
            if (value == null || value.trim().isEmpty()) {
                return "";
            }
            return value.split(",")[0].trim().split("-")[0].toLowerCase();
        }
    }
}
