package com.petadopt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * HybridSearchService 单元测试：聚焦 RRF 融合逻辑。
 */
@DisplayName("HybridSearchService 单元测试")
@ExtendWith(MockitoExtension.class)
class HybridSearchServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private EmbeddingService embeddingService;

    private HybridSearchService service;

    @BeforeEach
    void setUp() {
        service = new HybridSearchService(jdbcTemplate, embeddingService);
        when(embeddingService.generateEmbedding(anyString())).thenReturn(new float[4096]);
    }

    @Test
    @DisplayName("RRF 融合：去重 + 累加分数")
    void rrf_fuses_dedup_and_sum() {
        // 向量召回: [id=1 (rank0), id=2 (rank1)]
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(
                        Map.of("id", 1, "title", "t1", "content", "c1", "score", 0.9),
                        Map.of("id", 2, "title", "t2", "content", "c2", "score", 0.8)
                ));

        List<Map<String, Object>> results = service.search("测试", null, 5);

        assertThat(results).hasSize(2);
        // 第一名应该是 id=1（rank0）
        assertThat(results.get(0).get("id")).isEqualTo(1);
        assertThat(results.get(0)).containsKey("rrfScore");
    }

    @Test
    @DisplayName("召回全部失败 → 返回空列表（优雅降级）")
    void search_returns_empty_on_recall_failure() {
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenThrow(new RuntimeException("DB error"));

        List<Map<String, Object>> results = service.search("测试", null, 5);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("topK 限制生效")
    void topk_limit() {
        // 向量召回返回 10 条
        var rows = new java.util.ArrayList<Map<String, Object>>();
        for (int i = 1; i <= 10; i++) {
            rows.add(Map.of("id", i, "title", "t" + i, "content", "c" + i, "score", 1.0 - i * 0.01));
        }
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(rows);

        List<Map<String, Object>> results = service.search("q", null, 3);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).get("id")).isEqualTo(1);
    }
}