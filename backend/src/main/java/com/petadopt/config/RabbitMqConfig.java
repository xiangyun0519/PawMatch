package com.petadopt.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置：声明交换机、队列、绑定关系。
 *
 * - 匹配：pawmatch.exchange -> matching_queue
 * - 通知：pawmatch.exchange -> notification_queue
 * - 聊天（预留）：pawmatch.exchange -> chat_queue
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "pawmatch.exchange";

    public static final String MATCHING_QUEUE = "matching_queue";
    public static final String NOTIFICATION_QUEUE = "notification_queue";
    public static final String CHAT_QUEUE = "chat_queue";

    public static final String MATCHING_ROUTING_KEY = "matching";
    public static final String NOTIFICATION_ROUTING_KEY = "notification";
    public static final String CHAT_ROUTING_KEY = "chat";

    @Bean
    public DirectExchange pawmatchExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue matchingQueue() {
        return QueueBuilder.durable(MATCHING_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue chatQueue() {
        return QueueBuilder.durable(CHAT_QUEUE).build();
    }

    @Bean
    public Binding matchingBinding(Queue matchingQueue, DirectExchange pawmatchExchange) {
        return BindingBuilder.bind(matchingQueue).to(pawmatchExchange).with(MATCHING_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange pawmatchExchange) {
        return BindingBuilder.bind(notificationQueue).to(pawmatchExchange).with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, DirectExchange pawmatchExchange) {
        return BindingBuilder.bind(chatQueue).to(pawmatchExchange).with(CHAT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(EXCHANGE);
        return template;
    }
}