package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.dto.ChatRequest;
import com.petadopt.dto.ChatResponse;
import com.petadopt.entity.ChatMessage;
import com.petadopt.entity.ChatSession;
import com.petadopt.entity.PetProfile;
import com.petadopt.mapper.ChatMessageMapper;
import com.petadopt.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG 智能问答（混合检索版）。
 *
 * 流程：
 *  1. 加载最近 N 条历史消息 → 拼接为 query 上下文
 *  2. IntentClassifier 识别意图
 *  3. HybridSearchService 双路召回 + RRF 融合（top-5）
 *  4. 系统 prompt + 参考资料 + 用户问题 → LLM 生成
 *  5. 持久化消息、更新会话
 */
@Service
@Slf4j
public class ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final PetService petService;
    private final IntentClassifier intentClassifier;
    private final HybridSearchService hybridSearchService;
    private final ChatClient chatClient;

    public ChatService(ChatSessionMapper sessionMapper,
                       ChatMessageMapper messageMapper,
                       PetService petService,
                       IntentClassifier intentClassifier,
                       HybridSearchService hybridSearchService,
                       @Qualifier("dashScopeChatClient") ChatClient chatClient) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.petService = petService;
        this.intentClassifier = intentClassifier;
        this.hybridSearchService = hybridSearchService;
        this.chatClient = chatClient;
    }

    /** 单次回答最大注入文档数 */
    private static final int CONTEXT_TOP_K = 5;
    /** 历史消息数 */
    private static final int HISTORY_LIMIT = 6;

    public ChatResponse chat(Long userId, ChatRequest request) {
        ChatSession session;
        if (request.getSessionId() == null) {
            session = createSession(userId);
        } else {
            session = sessionMapper.selectById(request.getSessionId());
            if (session == null) session = createSession(userId);
        }

        saveMessage(session.getId(), "user", request.getMessage());

        String intent = intentClassifier.classify(request.getMessage());
        String response = generateResponse(userId, session.getId(), request.getMessage(), intent);

        saveMessage(session.getId(), "assistant", response);

        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        return ChatResponse.builder()
                .sessionId(session.getId())
                .message(response)
                .intent(intent)
                .build();
    }

    private ChatSession createSession(Long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    private void saveMessage(Long sessionId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
    }

    /**
     * 主生成：检索 + prompt 组装
     */
    private String generateResponse(Long userId, Long sessionId, String userMessage, String intent) {
        // 1. 加载历史（用于 query 改写与上下文注入）
        String historyText = loadHistoryText(sessionId);

        // 2. 混合检索：意图 → category 过滤
        String query = historyText.isEmpty() ? userMessage : historyText + "\n" + userMessage;
        String category = mapIntentToCategory(intent);

        List<Map<String, Object>> retrieved = hybridSearchService.search(query, category, CONTEXT_TOP_K);

        // 3. 额外：MATCH_CONSULT 时附带推荐宠物
        StringBuilder context = new StringBuilder();
        if (!retrieved.isEmpty()) {
            context.append("参考资料：\n");
            for (Map<String, Object> r : retrieved) {
                context.append("【").append(r.get("title")).append("】\n");
                context.append(safeGet(r, "content")).append("\n\n");
            }
        }
        if ("MATCH_CONSULT".equals(intent)) {
            List<PetProfile> pets = petService.getRecommendedPets(3);
            context.append("【推荐宠物】\n");
            for (PetProfile pet : pets) {
                context.append("- ").append(pet.getName())
                        .append("（").append(pet.getSpecies())
                        .append("，").append(formatAge(pet.getAgeMonths())).append("）\n");
            }
        }

        // 4. prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append(buildSystemPrompt(intent)).append("\n\n");
        if (!context.isEmpty()) {
            prompt.append(context).append("\n");
        }
        if (!historyText.isEmpty()) {
            prompt.append("对话历史：\n").append(historyText).append("\n\n");
        }
        prompt.append("用户问题：").append(userMessage).append("\n\n");
        prompt.append("请根据以上信息回答用户问题，回答要简洁友好，不超过200字。");

        return chatClient.prompt()
                .user(prompt.toString())
                .call()
                .content();
    }

    private String loadHistoryText(Long sessionId) {
        List<ChatMessage> history = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByDesc(ChatMessage::getCreatedAt)
                        .last("LIMIT " + HISTORY_LIMIT)
        );
        Collections.reverse(history);
        return history.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    private String mapIntentToCategory(String intent) {
        return switch (intent) {
            case "MATCH_CONSULT" -> "MATCH_CONSULT";
            case "CARE_KNOWLEDGE" -> "CARE_KNOWLEDGE";
            case "PROCESS" -> "PROCESS";
            default -> null; // GENERAL 不过滤
        };
    }

    private String buildSystemPrompt(String intent) {
        return switch (intent) {
            case "MATCH_CONSULT" -> "你是PawMatch的宠物领养顾问，帮助用户找到最适合的宠物。";
            case "CARE_KNOWLEDGE" -> "你是PawMatch的宠物护理专家，提供护理、喂养、训练、健康方面的建议。";
            case "PROCESS" -> "你是PawMatch的领养流程顾问，帮助用户了解领养流程与申请条件。";
            default -> "你是PawMatch的智能助手，友好地回答用户关于宠物领养的问题。";
        };
    }

    private String safeGet(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? "" : v.toString();
    }

    private String formatAge(int ageMonths) {
        if (ageMonths < 12) return ageMonths + "个月";
        int years = ageMonths / 12;
        int months = ageMonths % 12;
        return months == 0 ? years + "岁" : years + "岁" + months + "个月";
    }

    // ---------- 会话管理 ----------

    public List<ChatSession> getUserSessions(Long userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .orderByDesc(ChatSession::getUpdatedAt)
        );
    }

    public List<ChatMessage> getSessionMessages(Long sessionId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreatedAt)
        );
    }

    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session != null && session.getUserId().equals(userId)) {
            messageMapper.delete(
                    new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSessionId, sessionId)
            );
            sessionMapper.deleteById(sessionId);
        }
    }
}