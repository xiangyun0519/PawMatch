package com.petadopt.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通知消息消费者：将站内信写入 Redis（list）+ 持久化预留。
 *
 * key 设计：
 *  - notification:user:{userId}  List  最近通知
 *  - notification:unread:{userId} 计数器
 */
@Component
@Slf4j
public class NotificationConsumer {

    private final StringRedisTemplate redisTemplate;

    public NotificationConsumer(@org.springframework.beans.factory.annotation.Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = "notification_queue")
    public void handle(Map<String, Object> payload, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            Long userId = ((Number) payload.get("userId")).longValue();
            String type = String.valueOf(payload.getOrDefault("type", "INFO"));
            String content = String.valueOf(payload.getOrDefault("content", ""));
            String id = UUID.randomUUID().toString();

            Map<String, Object> note = Map.of(
                    "id", id,
                    "type", type,
                    "content", content,
                    "createdAt", System.currentTimeMillis()
            );

            String key = "notification:user:" + userId;
            String unreadKey = "notification:unread:" + userId;

            if (redisTemplate != null) {
                redisTemplate.opsForList().leftPush(key, note.toString());
                redisTemplate.opsForList().trim(key, 0, 99);  // 最多保留 100 条
                redisTemplate.expire(key, Duration.ofDays(30));
                redisTemplate.opsForValue().increment(unreadKey);
                redisTemplate.expire(unreadKey, Duration.ofDays(30));
            }

            log.info("通知已写入 userId={}, type={}", userId, type);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("通知处理失败", e);
            channel.basicAck(deliveryTag, false);
        }
    }
}