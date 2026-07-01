package com.petadopt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 意图识别：LLM 分类 + 关键词 fallback。
 *
 * 意图枚举：
 *  - MATCH_CONSULT    推荐/匹配咨询
 *  - CARE_KNOWLEDGE   护理/喂养/健康
 *  - PROCESS          领养流程/申请
 *  - GENERAL          兜底
 */
@Service
@Slf4j
public class IntentClassifier {

    private final ChatClient chatClient;  // 可为 null（未配置 DashScope 时降级到关键词）

    @Value("${intent.llm-enabled:true}")
    private boolean llmEnabled;

    public IntentClassifier(@org.springframework.beans.factory.annotation.Autowired(required = false)
                            @Qualifier("dashScopeChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
        if (chatClient == null) {
            log.warn("ChatClient 未注入，意图识别将仅使用关键词路径");
        }
    }

    /**
     * 主入口：分类用户输入的意图
     */
    public String classify(String message) {
        if (llmEnabled) {
            try {
                String intent = classifyByLlm(message);
                if (intent != null && !intent.isBlank()) return intent;
            } catch (Exception e) {
                log.warn("LLM 意图识别失败，降级到关键词：{}", e.getMessage());
            }
        }
        return classifyByKeyword(message);
    }

    private String classifyByLlm(String message) {
        String prompt = "你是宠物领养平台的意图分类器。请把用户问题归到以下类别之一：\n" +
                "- MATCH_CONSULT（咨询匹配、推荐、找合适的宠物）\n" +
                "- CARE_KNOWLEDGE（护理、喂养、训练、健康、疫苗、驱虫等知识）\n" +
                "- PROCESS（领养流程、申请条件、审核标准）\n" +
                "- GENERAL（其他）\n\n" +
                "用户问题：" + message + "\n\n" +
                "只输出类别名称，不要解释。";

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String classifyByKeyword(String message) {
        if (message == null || message.isBlank()) return "GENERAL";
        String m = message.toLowerCase(Locale.ROOT);

        if (containsAny(m, "匹配", "推荐", "适合", "领养哪", "想养")) return "MATCH_CONSULT";
        if (containsAny(m, "护理", "喂养", "训练", "健康", "疫苗", "驱虫", "绝育", "生病")) return "CARE_KNOWLEDGE";
        if (containsAny(m, "流程", "申请", "审核", "条件", "需要什么", "怎么办理")) return "PROCESS";
        return "GENERAL";
    }

    private boolean containsAny(String text, String... keywords) {
        List<String> list = Arrays.asList(keywords);
        return list.stream().anyMatch(text::contains);
    }
}