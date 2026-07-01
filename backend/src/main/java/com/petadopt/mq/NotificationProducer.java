package com.petadopt.mq;

import com.petadopt.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通知消息生产者。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(Long userId, String type, String content) {
        Map<String, Object> payload = Map.of(
                "userId", userId,
                "type", type,
                "content", content
        );
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.NOTIFICATION_ROUTING_KEY,
                payload
        );
        log.debug("通知已入队 userId={}, type={}", userId, type);
    }
}