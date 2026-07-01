package com.petadopt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petadopt.entity.MqMessageLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MqMessageLogMapper extends BaseMapper<MqMessageLog> {

    /**
     * 自定义 INSERT：直接传 JSON 字符串，让 PG ::jsonb 强转。
     * 避开 JacksonTypeHandler 在 INSERT 路径上的失效问题（MP 自动生成的 INSERT
     * 不会调用 typeHandler，把 Map 当成 varchar 直接写 jsonb 列报错）。
     */
    int insertRaw(@Param("messageId") String messageId,
                  @Param("businessType") String businessType,
                  @Param("payloadJson") String payloadJson,
                  @Param("status") String status,
                  @Param("retryCount") int retryCount,
                  @Param("businessId") Long businessId);

    /**
     * 自定义 UPDATE（标记 SUCCESS / FAILED 时回填结果快照）。
     */
    int updateResultSnapshot(@Param("id") Long id,
                             @Param("status") String status,
                             @Param("errorMessage") String errorMessage,
                             @Param("resultSnapshotJson") String resultSnapshotJson);

    /**
     * 自定义 UPDATE（重试计数 + 状态）。
     */
    int incrementRetry(@Param("id") Long id,
                       @Param("status") String status,
                       @Param("errorMessage") String errorMessage);
}