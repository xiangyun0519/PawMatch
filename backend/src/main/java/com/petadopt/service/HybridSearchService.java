package com.petadopt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合检索服务：向量召回 + BM25（tsvector）召回，RRF 融合。
 *
 * RRF (Reciprocal Rank Fusion):
 *   score(d) = sum( 1 / (k + rank_i(d)) )  for each retriever
 *   k 通常取 60；不需要归一化各路分数。
 *
 * 输出：去重后的 top-N（按 RRF 分数降序）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HybridSearchService {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;

    /** RRF k 常数 */
    private static final int RRF_K = 60;
    /** 每路召回数 */
    private static final int PER_RETRIEVER_LIMIT = 20;

    /**
     * 混合检索入口。
     *
     * @param query  用户查询
     * @param category 可选 category 过滤（MATCH_CONSULT / CARE_KNOWLEDGE / PROCESS）
     * @param topK    最终返回条数
     */
    public List<Map<String, Object>> search(String query, String category, int topK) {
        float[] qVec = embeddingService.generateEmbedding(query);
        String vectorStr = arrayToVectorString(qVec);

        // Stage 1: 双路并行召回
        List<Map<String, Object>> vectorHits = vectorRecall(vectorStr, category, PER_RETRIEVER_LIMIT);
        List<Map<String, Object>> bm25Hits = bm25Recall(query, category, PER_RETRIEVER_LIMIT);

        // Stage 2: RRF 融合
        Map<Long, Double> fused = new HashMap<>();
        Map<Long, Map<String, Object>> meta = new HashMap<>();

        applyRrf(vectorHits, fused, meta);
        applyRrf(bm25Hits, fused, meta);

        // Stage 3: 排序输出
        return fused.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topK)
                .map(e -> {
                    Map<String, Object> row = new HashMap<>(meta.get(e.getKey()));
                    row.put("rrfScore", e.getValue());
                    return row;
                })
                .collect(Collectors.toList());
    }

    // ---------- Stage 1: 向量召回（pgvector HNSW）----------

    private List<Map<String, Object>> vectorRecall(String vectorStr, String category, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, title, content, chunk_content, category, " +
                "1 - (embedding <=> ?::vector) AS score, 'vector' AS source " +
                "FROM knowledge_base WHERE embedding IS NOT NULL AND status = 'ACTIVE'"
        );
        List<Object> args = new ArrayList<>();
        args.add(vectorStr);
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            args.add(category);
        }
        sql.append(" ORDER BY embedding <=> ?::vector LIMIT ?");
        args.add(vectorStr);
        args.add(limit);

        try {
            return jdbcTemplate.queryForList(sql.toString(), args.toArray());
        } catch (Exception e) {
            log.warn("向量召回失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ---------- Stage 1: BM25 召回（PostgreSQL tsvector）----------

    private List<Map<String, Object>> bm25Recall(String query, String category, int limit) {
        // 简易 BM25：使用 ts_rank_cd 排序
        StringBuilder sql = new StringBuilder(
                "SELECT id, title, content, chunk_content, category, " +
                "ts_rank_cd(tsv, plainto_tsquery('simple', ?)) AS score, 'bm25' AS source " +
                "FROM knowledge_base " +
                "WHERE tsv @@ plainto_tsquery('simple', ?) AND status = 'ACTIVE'"
        );
        List<Object> args = new ArrayList<>();
        args.add(query);
        args.add(query);
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            args.add(category);
        }
        sql.append(" ORDER BY score DESC LIMIT ?");
        args.add(limit);

        try {
            return jdbcTemplate.queryForList(sql.toString(), args.toArray());
        } catch (Exception e) {
            log.warn("BM25 召回失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ---------- Stage 2: RRF 融合 ----------

    private void applyRrf(List<Map<String, Object>> hits, Map<Long, Double> fused, Map<Long, Map<String, Object>> meta) {
        for (int rank = 0; rank < hits.size(); rank++) {
            Map<String, Object> row = hits.get(rank);
            Long id = ((Number) row.get("id")).longValue();
            double rrfScore = 1.0 / (RRF_K + rank + 1);
            fused.merge(id, rrfScore, Double::sum);
            meta.putIfAbsent(id, row);
        }
    }

    // ---------- 工具 ----------

    private String arrayToVectorString(float[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}