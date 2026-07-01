package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.petadopt.entity.MqMessageLog;
import com.petadopt.mapper.MqMessageLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("MqMessageLogService 状态机测试")
@ExtendWith(MockitoExtension.class)
class MqMessageLogServiceTest {

    @Mock private MqMessageLogMapper mapper;
    private MqMessageLogService service;

    @BeforeEach
    void setUp() {
        service = new MqMessageLogService(mapper);
    }

    @Test
    @DisplayName("createPending：调用 insertRaw 并传 PENDING + JSON 字符串")
    void create_pending() {
        when(mapper.insertRaw(anyString(), anyString(), anyString(), anyString(), anyInt(), any()))
                .thenReturn(1);

        String messageId = service.createPending("MATCH_RECOMMEND", 100L, Map.of("key", "val"));

        ArgumentCaptor<String> messageIdCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> statusCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCap = ArgumentCaptor.forClass(String.class);

        org.mockito.Mockito.verify(mapper).insertRaw(
                messageIdCap.capture(), anyString(), jsonCap.capture(),
                statusCap.capture(), eq(0), eq(100L));

        assertThat(statusCap.getValue()).isEqualTo("PENDING");
        assertThat(messageIdCap.getValue()).isNotBlank();
        assertThat(jsonCap.getValue()).contains("\"key\"").contains("\"val\"");
    }

    @Test
    @DisplayName("markSuccess：调用 updateResultSnapshot 并传 SUCCESS")
    void mark_success() {
        MqMessageLog row = new MqMessageLog();
        row.setId(1L);
        row.setMessageId("mid");
        row.setRetryCount(0);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(row);

        service.markSuccess("mid", Map.of("count", 5));

        ArgumentCaptor<String> statusCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCap = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(mapper).updateResultSnapshot(
                eq(1L), statusCap.capture(), eq((String) null), jsonCap.capture());

        assertThat(statusCap.getValue()).isEqualTo("SUCCESS");
        assertThat(jsonCap.getValue()).contains("\"count\":5");
    }

    @Test
    @DisplayName("markFailed：调用 updateResultSnapshot 并传 FAILED + errorMessage")
    void mark_failed() {
        MqMessageLog row = new MqMessageLog();
        row.setId(2L);
        row.setMessageId("mid2");
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(row);

        service.markFailed("mid2", "boom");

        ArgumentCaptor<String> statusCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> errCap = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(mapper).updateResultSnapshot(
                eq(2L), statusCap.capture(), errCap.capture(), eq((String) null));

        assertThat(statusCap.getValue()).isEqualTo("FAILED");
        assertThat(errCap.getValue()).isEqualTo("boom");
    }

    @Test
    @DisplayName("incrementRetry：第 3 次仍失败 → 状态置 FAILED")
    void increment_reach_max_then_failed() {
        MqMessageLog row = new MqMessageLog();
        row.setId(3L);
        row.setMessageId("mid3");
        row.setRetryCount(2);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(row);

        service.incrementRetry("mid3", "still bad");

        ArgumentCaptor<String> statusCap = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(mapper).incrementRetry(eq(3L), statusCap.capture(), eq("still bad"));

        assertThat(statusCap.getValue()).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("incrementRetry：第一次失败 → 状态保留 PENDING")
    void increment_first_retry_still_pending() {
        MqMessageLog row = new MqMessageLog();
        row.setId(4L);
        row.setMessageId("mid4");
        row.setRetryCount(0);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(row);

        service.incrementRetry("mid4", "transient");

        ArgumentCaptor<String> statusCap = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(mapper).incrementRetry(eq(4L), statusCap.capture(), eq("transient"));

        assertThat(statusCap.getValue()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("markSuccess：messageId 不存在 → 安全跳过")
    void mark_success_not_found_safe() {
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(null);
        service.markSuccess("nope", Map.of());
        org.mockito.Mockito.verify(mapper, org.mockito.Mockito.never())
                .updateResultSnapshot(anyLong(), anyString(), anyString(), anyString());
    }
}