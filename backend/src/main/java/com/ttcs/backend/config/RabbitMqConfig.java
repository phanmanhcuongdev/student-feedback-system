package com.ttcs.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter messageConverter(JsonMapper objectMapper) {
        JsonMapper rabbitObjectMapper = objectMapper.rebuild()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        return new JacksonJsonMessageConverter(rabbitObjectMapper);
    }

    @Bean
    public RabbitTemplateCustomizer rabbitTemplateJsonMessageConverterCustomizer(MessageConverter messageConverter) {
        return rabbitTemplate -> rabbitTemplate.setMessageConverter(messageConverter);
    }

    @Bean
    public Queue translationRequestQueue(@Value("${app.rabbitmq.queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public DirectExchange translationRequestExchange(@Value("${app.rabbitmq.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding translationRequestBinding(
            Queue translationRequestQueue,
            DirectExchange translationRequestExchange,
            @Value("${app.rabbitmq.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(translationRequestQueue)
                .to(translationRequestExchange)
                .with(routingKey);
    }

    @Bean
    public Queue translationReplyQueue(@Value("${app.rabbitmq.reply-queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public DirectExchange translationReplyExchange(@Value("${app.rabbitmq.reply-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange translationReplyDeadLetterExchange(@Value("${app.rabbitmq.reply-dlx}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue translationReplyDeadLetterQueue(@Value("${app.rabbitmq.reply-dlq}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding translationReplyBinding(
            Queue translationReplyQueue,
            DirectExchange translationReplyExchange,
            @Value("${app.rabbitmq.reply-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(translationReplyQueue)
                .to(translationReplyExchange)
                .with(routingKey);
    }

    @Bean
    public Binding translationReplyDeadLetterBinding(
            Queue translationReplyDeadLetterQueue,
            DirectExchange translationReplyDeadLetterExchange,
            @Value("${app.rabbitmq.reply-dlq}") String routingKey
    ) {
        return BindingBuilder.bind(translationReplyDeadLetterQueue)
                .to(translationReplyDeadLetterExchange)
                .with(routingKey);
    }
}
