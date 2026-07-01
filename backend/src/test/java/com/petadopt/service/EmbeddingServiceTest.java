package com.petadopt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("EmbeddingService 维度探测单元测试")
@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock private EmbeddingModel embeddingModel;
    private EmbeddingService service;

    @BeforeEach
    void setUp() {
        service = new EmbeddingService(embeddingModel);
        TestUtils.setField(service, "embeddingModelName", "test-model");
        service.resetDimensionCache();
    }

    @Test
    @DisplayName("detectDimension 探测真实维度 4096")
    void detect_dimension_4096() {
        float[] probe = new float[4096];
        when(embeddingModel.embed("dim-probe")).thenReturn(probe);

        assertThat(service.detectDimension()).isEqualTo(4096);
        assertThat(service.getDimension()).isEqualTo(4096);  // 缓存
        // 二次调用不重复探测
        assertThat(service.detectDimension()).isEqualTo(4096);
    }

    @Test
    @DisplayName("detectDimension 探测维度 768")
    void detect_dimension_768() {
        float[] probe = new float[768];
        when(embeddingModel.embed("dim-probe")).thenReturn(probe);

        assertThat(service.detectDimension()).isEqualTo(768);
    }

    @Test
    @DisplayName("resetDimensionCache 清空后重新探测")
    void reset_cache() {
        when(embeddingModel.embed("dim-probe"))
                .thenReturn(new float[4096])
                .thenReturn(new float[1024]);

        assertThat(service.detectDimension()).isEqualTo(4096);
        service.resetDimensionCache();
        assertThat(service.detectDimension()).isEqualTo(1024);
    }

    @Test
    @DisplayName("formatAge: <12 月")
    void format_age_months() {
        assertThat(invokeFormatAge(6)).isEqualTo("6个月");
    }

    @Test
    @DisplayName("formatAge: 整年数")
    void format_age_years_only() {
        assertThat(invokeFormatAge(24)).isEqualTo("2岁");
    }

    @Test
    @DisplayName("formatAge: 年 + 月")
    void format_age_years_and_months() {
        assertThat(invokeFormatAge(30)).isEqualTo("2岁6个月");
    }

    // 反射调用 private 方法（仅测试用）
    private String invokeFormatAge(int months) {
        try {
            java.lang.reflect.Method m = EmbeddingService.class.getDeclaredMethod("formatAge", int.class);
            m.setAccessible(true);
            return (String) m.invoke(service, months);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}