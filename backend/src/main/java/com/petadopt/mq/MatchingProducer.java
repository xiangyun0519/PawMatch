package com.petadopt.mq;

import com.petadopt.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 匹配消息生产者：将匹配任务投递到 matching_queue。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(String messageId, Long adopterId, int topK) {
        Map<String, Object> payload = Map.of(
                "messageId", messageId,
                "adopterId", adopterId,
                "topK", topK
        );
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.MATCHING_ROUTING_KEY,
                payload
        );
        log.info("已投递匹配任务 messageId={}, adopterId={}", messageId, adopterId);
    }
}