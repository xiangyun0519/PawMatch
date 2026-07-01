package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MQ 消息追踪日志。
 *
 * 数据写入路径（自定义）：
 *  - payload / result_snapshot 由 MqMessageLogMapper.insertRaw / updateResultSnapshot 直接写
 *    入 PG jsonb 列（带 ::jsonb 强转），service 层手工序列化 Map 为 JSON String。
 *  - 这样规避 MyBatis-Plus 自动生成 INSERT SQL 时不会调用 JacksonTypeHandler 的问题。
 *
 * 数据读取路径：
 *  - SELECT 时 JacksonTypeHandler 自动把 jsonb 列反序列化为 Map<String,Object>。
 */
@Data
@TableName(value = "mq_message_log", autoResultMap = true)
public class MqMessageLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("message_id")
    private String messageId;

    @TableField("business_type")
    private String businessType;

    @TableField(value = "payload", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;

    private String status;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("error_message")
    private String errorMessage;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("processed_at")
    private LocalDateTime processedAt;

    @TableField("business_id")
    private Long businessId;

    @TableField(value = "result_snapshot", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> resultSnapshot;
}