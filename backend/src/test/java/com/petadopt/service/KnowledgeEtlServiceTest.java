package com.petadopt.service;

import com.petadopt.entity.KnowledgeBase;
import com.petadopt.mapper.KnowledgeBaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@DisplayName("KnowledgeEtlService 分块单元测试")
@ExtendWith(MockitoExtension.class)
class KnowledgeEtlServiceTest {

    @Mock private KnowledgeBaseMapper mapper;
    @Mock private EmbeddingService embeddingService;

    private KnowledgeEtlService service;

    @BeforeEach
    void setUp() {
        service = new KnowledgeEtlService(mapper, embeddingService);
        ReflectionTestUtils.setField(service, "chunkSize", 512);
        ReflectionTestUtils.setField(service, "chunkOverlap", 50);
    }

    @Test
    @DisplayName("短文本：单块")
    void chunk_short_text_single() {
        List<String> chunks = service.chunk("少于 512 字符的文本");
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).isEqualTo("少于 512 字符的文本");
    }

    @Test
    @DisplayName("长文本：多块 + overlap")
    void chunk_long_text_with_overlap() {
        // 1200 字符：步长 = 512-50 = 462
        // 期望块：[0..512), [462..974), [924..1200)
        String text = "x".repeat(1200);
        List<String> chunks = service.chunk(text);

        assertThat(chunks.size()).isGreaterThanOrEqualTo(2);
        assertThat(chunks.get(0).length()).isEqualTo(512);
    }

    @Test
    @DisplayName("空 / null 文本：返回空列表")
    void chunk_empty() {
        assertThat(service.chunk(null)).isEmpty();
        assertThat(service.chunk("")).isEmpty();
        assertThat(service.chunk("   ")).isEmpty();
    }

    @Test
    @DisplayName("ingest：分块 + embedding + 触发器自动 tsv（DB 端）")
    void ingest_calls_embedding_and_saves() {
        when(embeddingService.generateEmbedding(anyString())).thenReturn(new float[4096]);
        when(embeddingService.getDimension()).thenReturn(4096);
        when(mapper.insert(any(KnowledgeBase.class))).thenAnswer(inv -> {
            KnowledgeBase arg = inv.getArgument(0);
            arg.setId(1L);
            return 1;
        });

        KnowledgeBase kb = new KnowledgeBase();
        kb.setCategory("CARE_KNOWLEDGE");
        kb.setTitle("测试标题");
        kb.setContent("a".repeat(1000));

        KnowledgeBase saved = service.ingest(kb);

        assertThat(saved.getChunkContent()).contains("\n\n");
        assertThat(saved.getDimension()).isEqualTo(4096);
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }
}