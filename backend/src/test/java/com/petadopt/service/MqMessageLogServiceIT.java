package com.petadopt.service;

import com.petadopt.entity.MqMessageLog;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MqMessageLogService 集成测试：
 *  - 启动 H2 数据源 + MyBatis
 *  - 仅加载 Mapper 与 Service（不加载 Controller / Chat / Matching）
 */
@DisplayName("MqMessageLogService 集成测试")
@SpringBootTest(classes = MqMessageLogServiceIT.TestApp.class)
@ActiveProfiles("integration")
class MqMessageLogServiceIT {

    @Autowired private MqMessageLogService service;

    @org.springframework.boot.autoconfigure.SpringBootApplication
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            RabbitAutoConfiguration.class,
            RedisAutoConfiguration.class
    })
    @ComponentScan(
            basePackages = "com.petadopt",
            includeFilters = @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {MqMessageLogService.class, com.petadopt.mapper.MqMessageLogMapper.class}
            ),
            useDefaultFilters = false
    )
    static class TestApp {}

    @Test
    @DisplayName("createPending → findByMessageId → markSuccess 全链路")
    void full_lifecycle() {
        String mid = service.createPending("MATCH_RECOMMEND", 200L,
                Map.of("adopterId", 200, "topK", 5));

        MqMessageLog created = service.findByMessageId(mid);
        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo("PENDING");
        assertThat(created.getBusinessId()).isEqualTo(200L);

        service.markSuccess(mid, Map.of("count", 3));

        MqMessageLog after = service.findByMessageId(mid);
        assertThat(after.getStatus()).isEqualTo("SUCCESS");
        assertThat(after.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("incrementRetry 三次后转 FAILED")
    void retry_until_failed() {
        String mid = service.createPending("MATCH_RECOMMEND", 300L, Map.of());

        service.incrementRetry(mid, "err1");
        service.incrementRetry(mid, "err2");
        MqMessageLog after2 = service.findByMessageId(mid);
        assertThat(after2.getStatus()).isEqualTo("PENDING");
        assertThat(after2.getRetryCount()).isEqualTo(2);

        service.incrementRetry(mid, "err3");
        MqMessageLog after3 = service.findByMessageId(mid);
        assertThat(after3.getStatus()).isEqualTo("FAILED");
        assertThat(after3.getRetryCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("findByMessageId 不存在 → 返回 null")
    void not_found_returns_null() {
        MqMessageLog result = service.findByMessageId("non-existent");
        assertThat(result).isNull();
    }
}