package com.petadopt.service;

import com.petadopt.entity.AdopterProfile;
import com.petadopt.entity.PetProfile;
import com.petadopt.model.enums.ActivityLevel;
import com.petadopt.model.enums.PetExperience;
import com.petadopt.model.enums.PetSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * MatchingService 单元测试：聚焦打分逻辑与理由生成降级。
 */
@DisplayName("MatchingService 单元测试")
@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @InjectMocks
    private MatchingService service;

    @Mock private VectorSearchService vectorSearchService;
    @Mock private PetService petService;
    @Mock private AdopterProfileService adopterProfileService;
    @Mock private HardRuleFilter hardRuleFilter;
    @Mock private ChatClient chatClient;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    @BeforeEach
    void setUp() {
        // Redis 模板兜底（不让空指针）
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(valueOps.get(anyString())).thenReturn(null);

        // 注入 @Value 字段（反射）
        TestUtils.setField(service, "personalityWeight", 0.30);
        TestUtils.setField(service, "sizeWeight", 0.25);
        TestUtils.setField(service, "experienceWeight", 0.20);
        TestUtils.setField(service, "activityWeight", 0.15);
        TestUtils.setField(service, "healthWeight", 0.10);
        TestUtils.setField(service, "llmReasonEnabled", false);
    }

    // ---------- 模板理由 ----------

    @Test
    @DisplayName("模板理由：包含宠物名、分数、性格与健康")
    void template_reason_contains_key_fields() {
        AdopterProfile a = new AdopterProfile();
        a.setPreferredPetSize(List.of("MEDIUM"));
        a.setHousingType("HOUSE_WITH_YARD");

        PetProfile p = new PetProfile();
        p.setName("小黄");
        p.setSize("MEDIUM");
        p.setHealthStatus("健康");
        p.setPersonalityTags(List.of("活泼", "亲人"));

        String reason = service.buildTemplateReason(a, p, 85.0);

        assertThat(reason).contains("小黄");
        assertThat(reason).contains("85");
        assertThat(reason).contains("活泼");
        assertThat(reason).contains("健康");
    }

    @Test
    @DisplayName("模板理由：小公寓 + 小型 → 包含适合小户型文案")
    void template_reason_smallApt_smallSize() {
        AdopterProfile a = new AdopterProfile();
        a.setHousingType("SMALL_APARTMENT");
        a.setPreferredPetSize(List.of("SMALL"));

        PetProfile p = new PetProfile();
        p.setName("豆豆");
        p.setSize(PetSize.SMALL.getCode());
        p.setHealthStatus("健康");
        p.setPersonalityTags(List.of());

        String reason = service.buildTemplateReason(a, p, 90.0);
        assertThat(reason).contains("适合小户型");
    }

    // ---------- 主流程：召回 + 规则 + 排序 ----------

    @Test
    @DisplayName("主流程：召回为空 → 返回空列表")
    void match_returns_empty_when_no_candidates() {
        when(adopterProfileService.getProfileByUserId(1L)).thenReturn(profile());
        when(vectorSearchService.searchSimilarPets(anyLong(), anyInt())).thenReturn(List.of());

        List<MatchingService.MatchResult> results = service.matchPetsForAdopter(1L, 5);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("主流程：硬规则过滤后取 topK")
    void match_filters_and_sorts() {
        when(adopterProfileService.getProfileByUserId(1L)).thenReturn(profile());

        // 模拟 3 个候选，按相似度降序
        var candidate1 = java.util.Map.<String, Object>of("pet_id", 101L, "similarity", 0.9);
        var candidate2 = java.util.Map.<String, Object>of("pet_id", 102L, "similarity", 0.85);
        var candidate3 = java.util.Map.<String, Object>of("pet_id", 103L, "similarity", 0.80);
        when(vectorSearchService.searchSimilarPets(anyLong(), anyInt())).thenReturn(
                List.of(candidate1, candidate2, candidate3)
        );

        // pet 102 被硬规则过滤
        when(petService.getPetById(101L)).thenReturn(pet(101L, "MEDIUM", "DOG"));
        when(petService.getPetById(102L)).thenReturn(pet(102L, "SMALL", "DOG"));
        when(petService.getPetById(103L)).thenReturn(pet(103L, "MEDIUM", "CAT"));

        when(hardRuleFilter.passes(any(), any()))
                .thenReturn(true)
                .thenReturn(false)   // 102 被过滤
                .thenReturn(true);

        List<MatchingService.MatchResult> results = service.matchPetsForAdopter(1L, 5);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(MatchingService.MatchResult::getPetId)
                .containsExactly(101L, 103L);
        // 分数非空且在 0-100
        results.forEach(r -> {
            assertThat(r.getScore()).isNotNull();
            assertThat(r.getScore().doubleValue()).isBetween(0.0, 100.0);
        });
    }

    @Test
    @DisplayName("主流程：adopter profile 不存在 → 返回空")
    void match_returns_empty_when_no_profile() {
        when(adopterProfileService.getProfileByUserId(99L)).thenReturn(null);

        List<MatchingService.MatchResult> results = service.matchPetsForAdopter(99L, 5);
        assertThat(results).isEmpty();
    }

    // ---------- 工具 ----------

    private AdopterProfile profile() {
        AdopterProfile a = new AdopterProfile();
        a.setId(1L);
        a.setUserId(1L);
        a.setHousingType("HOUSE_WITH_YARD");
        a.setPetExperience(PetExperience.INTERMEDIATE.getCode());
        a.setActivityLevel(ActivityLevel.MODERATE.getCode());
        a.setPreferredPetSize(List.of("MEDIUM"));
        a.setPreferredPetAge(List.of("青年"));
        a.setAllergyInfo(null);
        a.setHasChildren(false);
        a.setHasElderly(false);
        a.setHasOtherPets(false);
        a.setDailyHoursAvailable(4);
        return a;
    }

    private PetProfile pet(Long id, String size, String species) {
        PetProfile p = new PetProfile();
        p.setId(id);
        p.setName("Pet-" + id);
        p.setSize(size);
        p.setSpecies(species);
        p.setAgeMonths(24);
        p.setGender("MALE");
        p.setHealthStatus("健康");
        p.setStatus("AVAILABLE");
        p.setPersonalityTags(List.of("亲人"));
        return p;
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
    private static long anyLong() {
        return org.mockito.ArgumentMatchers.anyLong();
    }
    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }
}