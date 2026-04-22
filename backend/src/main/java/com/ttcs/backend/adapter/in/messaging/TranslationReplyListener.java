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
                    message.translatedContent(),
                    message.sourceLang(),
                    message.targetLang(),
                    message.modelInfo(),
                    message.autoTranslated()
            ));
            if (!applied) {
                String errorMessage = "Translation reply was rejected by application service";
                log.error("{}: entityId={}, entityType={}, sourceLang={}, targetLang={}",
                        errorMessage, message.entityId(), message.entityType(), message.sourceLang(), message.targetLang());
                throw new AmqpRejectAndDontRequeueException(errorMessage);
            }
        } catch (RuntimeException exception) {
            log.error("Failed to process translation reply: entityId={}, entityType={}, sourceLang={}, targetLang={}",
                    message.entityId(), message.entityType(), message.sourceLang(), message.targetLang(), exception);
            if (exception instanceof AmqpRejectAndDontRequeueException) {
                throw exception;
            }
            throw exception;
        }
    }

    public record TranslationReplyMessage(
            @JsonProperty("entity_id") Integer entityId,
            @JsonProperty("entity_type") String entityType,
            @JsonProperty("translated_content") String translatedContent,
            @JsonProperty("source_lang") String sourceLang,
            @JsonProperty("target_lang") String targetLang,
            @JsonProperty("model_info") String modelInfo,
            @JsonProperty("is_auto_translated") boolean autoTranslated
    ) {
    }
}
