package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.petadopt.mapper.AdoptionApplicationMapper;
import com.petadopt.mapper.MqMessageLogMapper;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.ShelterMapper;
import com.petadopt.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * StatsService.getMatchingFunnel 单元测试。
 * 使用简单的连号 stub（按调用次数返回不同值）。
 */
@DisplayName("StatsService 匹配转化漏斗")
@ExtendWith(MockitoExtension.class)
class StatsServiceFunnelTest {

    @InjectMocks private StatsService service;

    @Mock private PetMapper petMapper;
    @Mock private AdoptionApplicationMapper applicationMapper;
    @Mock private ShelterMapper shelterMapper;
    @Mock private UserMapper userMapper;
    @Mock private MqMessageLogMapper mqMessageLogMapper;

    @Test
    @DisplayName("漏斗：四阶段计数 + 转化率")
    @SuppressWarnings("unchecked")
    void funnel_full() {
        // 用 Answer 按调用顺序返回不同值：
        //  - 第 1 次 applicationMapper.selectCount（总数）→ 50
        //  - 第 2 次（APPROVED）→ 20
        //  - 第 3 次（COMPLETED）→ 10
        when(mqMessageLogMapper.selectCount(any(Wrapper.class))).thenReturn(100L);

        java.util.concurrent.atomic.AtomicInteger callCount = new java.util.concurrent.atomic.AtomicInteger(0);
        when(applicationMapper.selectCount(any(Wrapper.class))).thenAnswer(inv -> {
            int n = callCount.incrementAndGet();
            if (n == 1) return 50L;   // 总申请数
            if (n == 2) return 20L;   // APPROVED
            return 10L;               // COMPLETED
        });

        Map<String, Object> funnel = service.getMatchingFunnel();

        List<Map<String, Object>> stages = (List<Map<String, Object>>) funnel.get("stages");
        assertThat(stages).hasSize(4);
        assertThat(stages.get(0).get("name")).isEqualTo("发起匹配");
        assertThat(stages.get(0).get("count")).isEqualTo(100L);
        assertThat(stages.get(1).get("count")).isEqualTo(50L);
        assertThat(stages.get(2).get("count")).isEqualTo(20L);
        assertThat(stages.get(3).get("count")).isEqualTo(10L);

        assertThat(stages.get(1).get("conversionFromPrev")).isEqualTo(50.0);
        assertThat(funnel.get("overallConversion")).isEqualTo(10.0);
    }

    @Test
    @DisplayName("漏斗：0 匹配 → 转化率全 0，不除零异常")
    void funnel_zero_match() {
        when(mqMessageLogMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(applicationMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        Map<String, Object> funnel = service.getMatchingFunnel();

        assertThat(funnel.get("overallConversion")).isEqualTo(0.0);
        List<Map<String, Object>> stages = (List<Map<String, Object>>) funnel.get("stages");
        assertThat(stages.get(1).get("conversionFromPrev")).isEqualTo(0.0);
    }
}