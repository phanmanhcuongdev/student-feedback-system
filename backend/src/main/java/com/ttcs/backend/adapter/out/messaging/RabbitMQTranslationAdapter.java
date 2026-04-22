package com.ttcs.backend.adapter.out.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ttcs.backend.application.port.out.ai.SendTranslationTaskPort;
import com.ttcs.backend.application.port.out.ai.TranslationTaskCommand;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQTranslationAdapter implements SendTranslationTaskPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQTranslationAdapter.class);

    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange translationRequestExchange;
    private final String routingKey;

    public RabbitMQTranslationAdapter(
            RabbitTemplate rabbitTemplate,
            @Qualifier("translationRequestExchange") DirectExchange translationRequestExchange,
            @Value("${app.rabbitmq.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.translationRequestExchange = translationRequestExchange;
        this.routingKey = routingKey;
    }

    @Override
    public void send(TranslationTaskCommand command) {
        rabbitTemplate.convertAndSend(translationRequestExchange.getName(), routingKey, TranslationTaskMessage.from(command));
        log.info("Published translation task: exchange={}, routingKey={}, entityId={}, entityType={}, sourceLang={}, targetLang={}",
                translationRequestExchange.getName(), routingKey, command.entityId(), command.entityType(), command.sourceLang(), command.targetLang());
    }

    private record TranslationTaskMessage(
            @JsonProperty("entity_id") Integer entityId,
            @JsonProperty("entity_type") String entityType,
            @JsonProperty("content") String content,
            @JsonProperty("source_lang") String sourceLang,
            @JsonProperty("target_lang") String targetLang
    ) {
        private static TranslationTaskMessage from(TranslationTaskCommand command) {
            return new TranslationTaskMessage(
                    command.entityId(),
                    command.entityType(),
                    command.content(),
                    command.sourceLang(),
                    command.targetLang()
            );
        }
    }
}
