package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.entity.KnowledgeBase;
import com.petadopt.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库 ETL：分块 → embedding → 入库。
 *
 * 分块策略（架构约定）：
 *  - chunk_size = 512 字符
 *  - overlap = 50 字符
 *
 * 对每条原始 KnowledgeBase：
 *  - chunkContent 字段保存所有 chunk 拼接（用 \n\n 分隔）
 *  - embedding 字段保存"全文"的向量（用于粗召回）
 *  - 触发器自动同步 tsv
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeEtlService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final EmbeddingService embeddingService;

    @Value("${knowledge.chunk-size:512}")
    private int chunkSize;

    @Value("${knowledge.chunk-overlap:50}")
    private int chunkOverlap;

    /**
     * 单条入库（已含分块 + embedding）
     */
    public KnowledgeBase ingest(KnowledgeBase kb) {
        List<String> chunks = chunk(kb.getContent());
        String joined = String.join("\n\n", chunks);
        kb.setChunkContent(joined);

        // embedding 用 chunk 拼接后的全文，保持与 RAG 检索一致
        float[] vec = embeddingService.generateEmbedding(joined);
        kb.setEmbedding(vec);
        kb.setModel("Qwen/Qwen3-Embedding-8B");
        kb.setDimension(embeddingService.getDimension());
        kb.setStatus("ACTIVE");
        kb.setCreatedAt(LocalDateTime.now());
        kb.setUpdatedAt(LocalDateTime.now());

        if (kb.getId() == null) {
            knowledgeBaseMapper.insert(kb);
        } else {
            knowledgeBaseMapper.updateById(kb);
        }
        return kb;
    }

    /**
     * 批量重建：清空知识库后全量重算 embedding（用于模型切换场景）。
     */
    public int rebuildAll(List<KnowledgeBase> knowledgeList) {
        int count = 0;
        for (KnowledgeBase kb : knowledgeList) {
            try {
                ingest(kb);
                count++;
            } catch (Exception e) {
                log.error("知识 {} 入库失败：{}", kb.getTitle(), e.getMessage());
            }
        }
        log.info("知识库批量重建完成，共 {} 条", count);
        return count;
    }

    /**
     * 文本分块：固定大小 + overlap。
     * 中文按字符切分；不做句子切分（保证简单可复现）。
     */
    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<String> chunks = new ArrayList<>();
        int step = Math.max(1, chunkSize - chunkOverlap);
        for (int i = 0; i < text.length(); i += step) {
            int end = Math.min(text.length(), i + chunkSize);
            String piece = text.substring(i, end);
            if (!piece.isBlank()) chunks.add(piece);
            if (end >= text.length()) break;
        }
        return chunks;
    }

    /**
     * 列出所有知识（用于管理员后台）
     */
    public List<KnowledgeBase> listAll() {
        return knowledgeBaseMapper.selectList(
                new LambdaQueryWrapper<KnowledgeBase>().orderByDesc(KnowledgeBase::getId)
        );
    }

    public void delete(Long id) {
        knowledgeBaseMapper.deleteById(id);
    }
}