package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.petadopt.entity.MqMessageLog;
import com.petadopt.mapper.MqMessageLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * MQ 消息日志服务。
 *
 * 设计：service 层手工把 Map 序列化为 JSON String，再调用 mapper.insertRaw 写入
 * （INSERT 用 #{payloadJson}::jsonb 强转，避免 MyBatis-Plus 自动生成 SQL 把 Map
 * 当成 varchar）。SELECT 仍由 JacksonTypeHandler 反序列化为 Map。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MqMessageLogService {

    private final MqMessageLogMapper mqMessageLogMapper;
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    public String createPending(String businessType, Long businessId, Map<String, Object> payload) {
        String messageId = UUID.randomUUID().toString();
        mqMessageLogMapper.insertRaw(
                messageId,
                businessType,
                toJson(payload),
                "PENDING",
                0,
                businessId
        );
        return messageId;
    }

    public void markSuccess(String messageId, Map<String, Object> resultSnapshot) {
        MqMessageLog row = findByMessageId(messageId);
        if (row == null) return;
        mqMessageLogMapper.updateResultSnapshot(
                row.getId(),
                "SUCCESS",
                null,
                toJson(resultSnapshot)
        );
    }

    public void markFailed(String messageId, String errorMessage) {
        MqMessageLog row = findByMessageId(messageId);
        if (row == null) return;
        mqMessageLogMapper.updateResultSnapshot(
                row.getId(),
                "FAILED",
                errorMessage,
                null
        );
    }

    public void incrementRetry(String messageId, String errorMessage) {
        MqMessageLog row = findByMessageId(messageId);
        if (row == null) return;
        int newCount = (row.getRetryCount() == null ? 0 : row.getRetryCount()) + 1;
        String finalStatus = newCount >= 3 ? "FAILED" : "PENDING";
        mqMessageLogMapper.incrementRetry(row.getId(), finalStatus, errorMessage);
    }

    public MqMessageLog findByMessageId(String messageId) {
        return mqMessageLogMapper.selectOne(
                new LambdaQueryWrapper<MqMessageLog>().eq(MqMessageLog::getMessageId, messageId)
        );
    }

    private static String toJson(Object o) {
        if (o == null) return null;
        try {
            return MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.warn("JSON 序列化失败: {}", e.getMessage());
            return "{}";
        }
    }
}