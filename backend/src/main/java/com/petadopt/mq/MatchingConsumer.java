package com.petadopt.mq;

import com.petadopt.entity.MqMessageLog;
import com.petadopt.service.AdopterProfileService;
import com.petadopt.service.MatchingService;
import com.petadopt.service.MqMessageLogService;
import com.petadopt.service.MatchingService.MatchResult;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 匹配消息消费者：从 matching_queue 消费匹配任务。
 *
 * 失败策略：捕获异常后调用 mqMessageLogService.incrementRetry 计数；
 * 超过 3 次后由 service 标记 FAILED。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingConsumer {

    private final MatchingService matchingService;
    private final MqMessageLogService mqMessageLogService;
    private final AdopterProfileService adopterProfileService;

    @RabbitListener(queues = "matching_queue")
    public void handle(Map<String, Object> payload, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = (String) payload.get("messageId");
        Long adopterId = ((Number) payload.get("adopterId")).longValue();
        int topK = ((Number) payload.get("topK")).intValue();

        try {
            log.info("开始处理匹配任务 messageId={}, adopterId={}", messageId, adopterId);
            List<MatchResult> results = matchingService.matchPetsForAdopter(adopterId, topK);

            // 异步生成理由（不阻塞响应；失败由 service 内置降级）
            try {
                var profile = adopterProfileService.getProfileByUserId(adopterId);
                for (MatchResult r : results) {
                    String reason = matchingService.generateReason(
                            adopterId, r.getPetId(), profile, r.getPet(),
                            r.getScore() == null ? 0 : r.getScore().doubleValue()
                    );
                    r.setReasons(reason);
                }
            } catch (Exception e) {
                log.warn("异步生成匹配理由部分失败：{}", e.getMessage());
            }

            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("adopterId", adopterId);
            snapshot.put("topK", topK);
            snapshot.put("count", results.size());
            snapshot.put("results", results);

            mqMessageLogService.markSuccess(messageId, snapshot);
            channel.basicAck(deliveryTag, false);
            log.info("匹配任务完成 messageId={}, count={}", messageId, results.size());

        } catch (Exception e) {
            log.error("匹配任务失败 messageId={}", messageId, e);
            MqMessageLog logRow = mqMessageLogService.findByMessageId(messageId);
            if (logRow != null && logRow.getRetryCount() != null && logRow.getRetryCount() >= 2) {
                mqMessageLogService.markFailed(messageId, e.getMessage());
            } else {
                mqMessageLogService.incrementRetry(messageId, e.getMessage());
            }
            channel.basicAck(deliveryTag, false);
        }
    }
}