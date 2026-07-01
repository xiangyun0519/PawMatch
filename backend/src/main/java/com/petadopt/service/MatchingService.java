package com.petadopt.service;

import com.petadopt.entity.AdopterProfile;
import com.petadopt.entity.PetProfile;
import com.petadopt.model.enums.ActivityLevel;
import com.petadopt.model.enums.PetExperience;
import com.petadopt.model.enums.PetSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

/**
 * 匹配服务（核心）。
 *
 * 流水线：
 *  1. 向量召回 topK*2 候选宠物
 *  2. 硬规则过滤（HardRuleFilter）
 *  3. 6 维加权精排打分
 *  4. 匹配理由生成（异步 + 缓存 + 模板降级）
 */
@Service
@Slf4j
public class MatchingService {

    private final VectorSearchService vectorSearchService;
    private final PetService petService;
    private final AdopterProfileService adopterProfileService;
    private final HardRuleFilter hardRuleFilter;
    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;

    /** 缓存：匹配理由 Redis key 前缀（同 adopterId+petId 复用） */
    private static final String REASON_CACHE_PREFIX = "match:reason:";
    /** 理由缓存 TTL 7 天 */
    private static final Duration REASON_CACHE_TTL = Duration.ofDays(7);

    @Value("${matching.weights.personality_match:0.30}")
    private double personalityWeight;
    @Value("${matching.weights.size_preference:0.25}")
    private double sizeWeight;
    @Value("${matching.weights.experience_match:0.20}")
    private double experienceWeight;
    @Value("${matching.weights.activity_match:0.15}")
    private double activityWeight;
    @Value("${matching.weights.health_status:0.10}")
    private double healthWeight;

    /** 是否启用 LLM 生成理由（false 则模板降级） */
    @Value("${matching.reason.llm-enabled:true}")
    private boolean llmReasonEnabled;

    public MatchingService(VectorSearchService vectorSearchService,
                            PetService petService,
                            AdopterProfileService adopterProfileService,
                            HardRuleFilter hardRuleFilter,
                            @Qualifier("dashScopeChatClient") ChatClient chatClient,
                            StringRedisTemplate redisTemplate) {
        this.vectorSearchService = vectorSearchService;
        this.petService = petService;
        this.adopterProfileService = adopterProfileService;
        this.hardRuleFilter = hardRuleFilter;
        this.chatClient = chatClient;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 主入口：召回 → 规则 → 精排
     */
    public List<MatchResult> matchPetsForAdopter(Long adopterId, int topK) {
        AdopterProfile profile = adopterProfileService.getProfileByUserId(adopterId);
        if (profile == null) {
            log.warn("领养人画像不存在 adopterId={}", adopterId);
            return Collections.emptyList();
        }

        List<Map<String, Object>> similarPets = vectorSearchService.searchSimilarPets(profile.getId(), topK * 2);

        List<MatchResult> results = new ArrayList<>();
        for (Map<String, Object> petData : similarPets) {
            Long petId = ((Number) petData.get("pet_id")).longValue();
            double similarity = ((Number) petData.get("similarity")).doubleValue();

            PetProfile pet = petService.getPetById(petId);

            if (!hardRuleFilter.passes(profile, pet)) {
                log.debug("宠物 {} 被硬规则过滤", petId);
                continue;
            }

            double finalScore = calculateFinalScore(profile, pet, similarity);

            MatchResult result = new MatchResult();
            result.setPetId(petId);
            result.setPetName(pet.getName());
            result.setScore(BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP));
            result.setPet(pet);
            // 维度打分明细（用于前端展示与调试）
            result.setDimensionScores(buildDimensionScores(profile, pet, similarity));

            results.add(result);
        }

        results.sort((a, b) -> b.getScore().compareTo(a.getScore()));
        return results.subList(0, Math.min(topK, results.size()));
    }

    /**
     * 生成匹配理由（异步或同步调用均可）。
     * 优先 Redis 缓存 → 其次 LLM → 模板降级。
     */
    public String generateReason(Long adopterId, Long petId, AdopterProfile adopter, PetProfile pet, double score) {
        String cacheKey = REASON_CACHE_PREFIX + adopterId + ":" + petId;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis 读取理由缓存失败，降级到模板: {}", e.getMessage());
        }

        String reason;
        if (llmReasonEnabled) {
            try {
                reason = generateReasonByLlm(adopter, pet, score);
            } catch (Exception e) {
                log.warn("LLM 生成理由失败，降级到模板: {}", e.getMessage());
                reason = buildTemplateReason(adopter, pet, score);
            }
        } else {
            reason = buildTemplateReason(adopter, pet, score);
        }

        try {
            redisTemplate.opsForValue().set(cacheKey, reason, REASON_CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis 写入理由缓存失败: {}", e.getMessage());
        }
        return reason;
    }

    // ---------- 内部：打分 ----------

    private double calculateFinalScore(AdopterProfile profile, PetProfile pet, double vectorSimilarity) {
        // 6 维加权；向量相似度作为先验折扣（×0.3 注入 personality 维度）
        double personality = clamp01(vectorSimilarity);
        double size = computeSizeScore(profile, pet);
        double experience = computeExperienceScore(profile, pet);
        double activity = computeActivityScore(profile, pet);
        double health = computeHealthScore(pet);

        double raw = personality * personalityWeight
                + size * sizeWeight
                + experience * experienceWeight
                + activity * activityWeight
                + health * healthWeight;

        return Math.min(raw * 100, 100.0);
    }

    private Map<String, Double> buildDimensionScores(AdopterProfile profile, PetProfile pet, double similarity) {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("personality_match", clamp01(similarity));
        m.put("size_preference", computeSizeScore(profile, pet));
        m.put("experience_match", computeExperienceScore(profile, pet));
        m.put("activity_match", computeActivityScore(profile, pet));
        m.put("health_status", computeHealthScore(pet));
        return m;
    }

    private double computeSizeScore(AdopterProfile profile, PetProfile pet) {
        if (profile.getPreferredPetSize() == null || profile.getPreferredPetSize().isEmpty()) return 0.6;
        return profile.getPreferredPetSize().contains(pet.getSize()) ? 1.0 : 0.2;
    }

    private double computeExperienceScore(AdopterProfile profile, PetProfile pet) {
        String exp = profile.getPetExperience();
        if (exp == null) return 0.5;
        PetExperience pe = PetExperience.fromCode(exp);
        return switch (pe) {
            case EXPERT -> 1.0;
            case INTERMEDIATE -> pet.getAgeMonths() == null || pet.getAgeMonths() <= 12 ? 0.7 : 1.0;
            case BEGINNER -> pet.getAgeMonths() != null && pet.getAgeMonths() > 12 && "健康".equals(pet.getHealthStatus()) ? 0.9 : 0.5;
            case NONE -> 0.4;
        };
    }

    private double computeActivityScore(AdopterProfile profile, PetProfile pet) {
        if (profile.getActivityLevel() == null || pet.getPersonalityTags() == null) return 0.5;
        ActivityLevel al = ActivityLevel.fromCode(profile.getActivityLevel());
        boolean activePet = pet.getPersonalityTags().stream().anyMatch(t ->
                t != null && (t.contains("活泼") || t.contains("爱动") || t.contains("精力旺盛")));
        if (al == ActivityLevel.HIGH) return activePet ? 1.0 : 0.4;
        if (al == ActivityLevel.LOW) return activePet ? 0.3 : 0.9;
        return 0.7;
    }

    private double computeHealthScore(PetProfile pet) {
        if (pet.getHealthStatus() == null) return 0.6;
        String h = pet.getHealthStatus();
        if (h.contains("健康")) return 1.0;
        if (h.contains("康复") || h.contains("治疗中")) return 0.5;
        return 0.3;
    }

    private double clamp01(double v) {
        if (Double.isNaN(v)) return 0;
        return Math.max(0, Math.min(1, v));
    }

    // ---------- 内部：理由生成 ----------

    private String generateReasonByLlm(AdopterProfile profile, PetProfile pet, double score) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个宠物领养匹配专家。请用简洁中文（不超过100字）解释为什么这只宠物适合这个领养人。\n\n");
        prompt.append("领养人：住房=").append(profile.getHousingType())
                .append("，经验=").append(profile.getPetExperience())
                .append("，活动=").append(profile.getActivityLevel()).append("\n");
        if (profile.getAdoptionMotivation() != null) {
            prompt.append("动机：").append(profile.getAdoptionMotivation()).append("\n");
        }
        prompt.append("\n宠物：").append(pet.getName()).append("，")
                .append(pet.getSpecies()).append("，")
                .append(pet.getBreed()).append("，")
                .append(pet.getAgeMonths()).append("月，")
                .append(pet.getSize()).append("，")
                .append("性格=").append(pet.getPersonalityTags()).append("\n");
        prompt.append("匹配分数：").append(String.format("%.1f", score)).append("\n");
        prompt.append("请给理由：");

        return chatClient.prompt()
                .user(prompt.toString())
                .call()
                .content();
    }

    /**
     * T23 模板降级（零 LLM 成本）。
     */
    String buildTemplateReason(AdopterProfile profile, PetProfile pet, double score) {
        StringBuilder sb = new StringBuilder();
        sb.append("根据您的画像，「").append(pet.getName()).append("」与您的匹配度为 ")
                .append(String.format("%.0f", score)).append(" 分。");

        if (profile.getPreferredPetSize() != null && profile.getPreferredPetSize().contains(pet.getSize())) {
            sb.append("体型符合您").append(pet.getSize()).append("的偏好；");
        }
        if (pet.getPersonalityTags() != null && !pet.getPersonalityTags().isEmpty()) {
            sb.append("性格").append(String.join("、", pet.getPersonalityTags())).append("；");
        }
        if (pet.getHealthStatus() != null && pet.getHealthStatus().contains("健康")) {
            sb.append("健康状况良好；");
        }
        if ("SMALL_APARTMENT".equalsIgnoreCase(profile.getHousingType())
                && PetSize.SMALL.getCode().equalsIgnoreCase(pet.getSize())) {
            sb.append("适合小户型。");
        }
        return sb.toString();
    }

    @lombok.Data
    public static class MatchResult {
        private Long petId;
        private String petName;
        private BigDecimal score;
        private String reasons;
        private PetProfile pet;
        private Map<String, Double> dimensionScores;
    }
}