package com.petadopt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * IntentClassifier 单元测试。
 * 策略：关闭 LLM 路径（关键词路径），加上开启 LLM 时异常降级到关键词的覆盖。
 */
@DisplayName("意图识别单元测试")
@ExtendWith(MockitoExtension.class)
class IntentClassifierTest {

    @Mock private ChatClient chatClient;
    private IntentClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new IntentClassifier(chatClient);
        TestUtils.setField(classifier, "llmEnabled", false);  // 默认走关键词
    }

    @Test
    @DisplayName("关键词：'推荐适合' → MATCH_CONSULT")
    void keyword_match_consult() {
        assertThat(classifier.classify("推荐适合我的宠物")).isEqualTo("MATCH_CONSULT");
    }

    @Test
    @DisplayName("关键词：'疫苗' → CARE_KNOWLEDGE")
    void keyword_care_vaccine() {
        assertThat(classifier.classify("小猫什么时候打疫苗？")).isEqualTo("CARE_KNOWLEDGE");
    }

    @Test
    @DisplayName("关键词：'喂养' → CARE_KNOWLEDGE")
    void keyword_care_feed() {
        assertThat(classifier.classify("幼犬喂养注意事项")).isEqualTo("CARE_KNOWLEDGE");
    }

    @Test
    @DisplayName("关键词：'领养流程' → PROCESS")
    void keyword_process() {
        assertThat(classifier.classify("请问领养流程是怎样的？")).isEqualTo("PROCESS");
    }

    @Test
    @DisplayName("关键词：未命中 → GENERAL")
    void keyword_general() {
        assertThat(classifier.classify("你好")).isEqualTo("GENERAL");
        assertThat(classifier.classify("")).isEqualTo("GENERAL");
        assertThat(classifier.classify(null)).isEqualTo("GENERAL");
    }

    @Test
    @DisplayName("LLM 异常 → 自动降级到关键词")
    void llm_exception_falls_back_to_keyword() {
        TestUtils.setField(classifier, "llmEnabled", true);
        when(chatClient.prompt(anyString())).thenThrow(new RuntimeException("API down"));

        assertThat(classifier.classify("推荐适合")).isEqualTo("MATCH_CONSULT");
    }
}