package com.petadopt.service;

import com.petadopt.entity.AdopterProfile;
import com.petadopt.entity.PetProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HardRuleFilter 单元测试（覆盖规则 5 条）。
 */
@DisplayName("硬规则过滤器单元测试")
class HardRuleFilterTest {

    private HardRuleFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HardRuleFilter();
    }

    private AdopterProfile adopter(String housing, String allergy, List<String> prefs) {
        AdopterProfile a = new AdopterProfile();
        a.setHousingType(housing);
        a.setAllergyInfo(allergy);
        a.setPreferredPetSize(prefs);
        a.setHasChildren(false);
        a.setHasElderly(false);
        a.setHasOtherPets(false);
        return a;
    }

    private PetProfile pet(String species, String size, String status) {
        PetProfile p = new PetProfile();
        p.setSpecies(species);
        p.setSize(size);
        p.setStatus(status);
        p.setAgeMonths(24);
        return p;
    }

    // ---------- R1 过敏规则 ----------

    @Test
    @DisplayName("R1 用户对猫过敏 → 排除猫")
    void passes_allergy_cat_excludes_cat() {
        AdopterProfile a = adopter("HOUSE_WITH_YARD", "猫毛过敏", null);
        PetProfile cat = pet("CAT", "SMALL", "AVAILABLE");
        assertThat(filter.passesAllergyRule(a, cat)).isFalse();
    }

    @Test
    @DisplayName("R1 用户对猫过敏 → 不排除狗")
    void passes_allergy_cat_allows_dog() {
        AdopterProfile a = adopter("HOUSE_WITH_YARD", "猫毛过敏", null);
        PetProfile dog = pet("DOG", "MEDIUM", "AVAILABLE");
        assertThat(filter.passesAllergyRule(a, dog)).isTrue();
    }

    @Test
    @DisplayName("R1 关键词命中英文 cat")
    void passes_allergy_english_keyword() {
        AdopterProfile a = adopter("HOUSE_WITH_YARD", "allergic to cat hair", null);
        PetProfile cat = pet("CAT", "SMALL", "AVAILABLE");
        assertThat(filter.passesAllergyRule(a, cat)).isFalse();
    }

    @Test
    @DisplayName("R1 无过敏信息 → 全部放行")
    void passes_allergy_none_passes_all() {
        AdopterProfile a = adopter("HOUSE_WITH_YARD", null, null);
        assertThat(filter.passesAllergyRule(a, pet("CAT", "SMALL", "AVAILABLE"))).isTrue();
        assertThat(filter.passesAllergyRule(a, pet("DOG", "LARGE", "AVAILABLE"))).isTrue();
    }

    // ---------- R2 住房 vs 体型 ----------

    @Test
    @DisplayName("R2 小公寓 + 大型犬 → 排除")
    void passes_housing_smallApt_largeDog_excluded() {
        AdopterProfile a = adopter("SMALL_APARTMENT", null, null);
        PetProfile p = pet("DOG", "LARGE", "AVAILABLE");
        assertThat(filter.passesHousingSizeRule(a, p)).isFalse();
    }

    @Test
    @DisplayName("R2 小公寓 + 小型犬 → 放行")
    void passes_housing_smallApt_smallDog_passes() {
        AdopterProfile a = adopter("SMALL_APARTMENT", null, null);
        PetProfile p = pet("DOG", "SMALL", "AVAILABLE");
        assertThat(filter.passesHousingSizeRule(a, p)).isTrue();
    }

    @Test
    @DisplayName("R2 小公寓 + 大型猫 → 放行（猫不受此规则约束）")
    void passes_housing_smallApt_largeCat_passes() {
        AdopterProfile a = adopter("SMALL_APARTMENT", null, null);
        PetProfile p = pet("CAT", "LARGE", "AVAILABLE");
        assertThat(filter.passesHousingSizeRule(a, p)).isTrue();
    }

    @Test
    @DisplayName("R2 大公寓 + 大型犬 → 放行")
    void passes_housing_house_largeDog_passes() {
        AdopterProfile a = adopter("HOUSE_WITH_YARD", null, null);
        PetProfile p = pet("DOG", "LARGE", "AVAILABLE");
        assertThat(filter.passesHousingSizeRule(a, p)).isTrue();
    }

    // ---------- R4 状态 ----------

    @Test
    @DisplayName("R4 AVAILABLE 通过")
    void passes_status_available_passes() {
        assertThat(filter.passesStatusRule(pet("DOG", "MEDIUM", "AVAILABLE"))).isTrue();
    }

    @Test
    @DisplayName("R4 ADOPTED 拒绝")
    void passes_status_adopted_excluded() {
        assertThat(filter.passesStatusRule(pet("DOG", "MEDIUM", "ADOPTED"))).isFalse();
    }

    @Test
    @DisplayName("R4 PENDING 拒绝")
    void passes_status_pending_excluded() {
        assertThat(filter.passesStatusRule(pet("DOG", "MEDIUM", "PENDING"))).isFalse();
    }

    // ---------- R5 偏好排斥 ----------

    @Test
    @DisplayName("R5 偏好包含 NONE_LARGE → 排除大型")
    void passes_preference_excludes_large() {
        AdopterProfile a = adopter("HOUSE_WITH_YARD", null, List.of("NONE_LARGE", "SMALL"));
        assertThat(filter.passesPreferenceExclusion(a, pet("DOG", "LARGE", "AVAILABLE"))).isFalse();
    }

    @Test
    @DisplayName("R5 偏好不冲突 → 放行")
    void passes_preference_no_conflict() {
        AdopterProfile a = adopter("HOUSE_WITH_YARD", null, List.of("SMALL", "MEDIUM"));
        assertThat(filter.passesPreferenceExclusion(a, pet("DOG", "LARGE", "AVAILABLE"))).isTrue();
    }

    // ---------- passes 主入口 ----------

    @Test
    @DisplayName("主入口：所有规则通过")
    void passes_all_passes() {
        AdopterProfile a = adopter("HOUSE_WITH_YARD", null, null);
        PetProfile p = pet("DOG", "MEDIUM", "AVAILABLE");
        assertThat(filter.passes(a, p)).isTrue();
    }

    @Test
    @DisplayName("主入口：任一规则失败即排除")
    void passes_any_rule_failed_excludes() {
        AdopterProfile a = adopter("SMALL_APARTMENT", null, null);
        PetProfile p = pet("DOG", "LARGE", "AVAILABLE");  // R2 失败
        assertThat(filter.passes(a, p)).isFalse();
    }
}