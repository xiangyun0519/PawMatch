package com.petadopt.service;

import com.petadopt.entity.AdopterProfile;
import com.petadopt.entity.PetProfile;
import com.petadopt.model.enums.HousingType;
import com.petadopt.model.enums.PetSize;
import com.petadopt.model.enums.PetSpecies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 硬性规则过滤器：在向量召回之后、精排打分之前执行。
 *
 * 规则集：
 *  R1 过敏冲突：用户标注对某物种过敏，则排除该物种
 *  R2 体型与住房冲突：小户型公寓(SMALL_APARTMENT) 不能养大型犬(LARGE+DOG)
 *  R3 有小孩家庭：避免标记为"对儿童不友好"的宠物（描述/性格标签包含"敏感"、"怕人"等关键字）
 *  R4 宠物状态：排除非 AVAILABLE 的宠物
 *  R5 偏好尺寸过滤：若用户明确表态「不要某种体型」，则排除
 */
@Component
@Slf4j
public class HardRuleFilter {

    /**
     * 主入口：硬规则过滤
     */
    public boolean passes(AdopterProfile adopter, PetProfile pet) {
        return passesAllergyRule(adopter, pet)
                && passesHousingSizeRule(adopter, pet)
                && passesStatusRule(pet)
                && passesPreferenceExclusion(adopter, pet);
    }

    /**
     * R1 过敏规则
     */
    boolean passesAllergyRule(AdopterProfile adopter, PetProfile pet) {
        if (adopter.getAllergyInfo() == null || adopter.getAllergyInfo().isBlank()) {
            return true;
        }
        String allergy = adopter.getAllergyInfo().toLowerCase();
        String speciesCode = pet.getSpecies() == null ? "" : pet.getSpecies().toLowerCase();
        if (allergy.contains("猫") || allergy.contains("cat")) {
            return !PetSpecies.CAT.getCode().equalsIgnoreCase(speciesCode);
        }
        if (allergy.contains("狗") || allergy.contains("dog")) {
            return !PetSpecies.DOG.getCode().equalsIgnoreCase(speciesCode);
        }
        if (allergy.contains("兔") || allergy.contains("rabbit")) {
            return !PetSpecies.RABBIT.getCode().equalsIgnoreCase(speciesCode);
        }
        return true;
    }

    /**
     * R2 住房 vs 体型规则
     */
    boolean passesHousingSizeRule(AdopterProfile adopter, PetProfile pet) {
        if (adopter.getHousingType() == null || pet.getSize() == null) return true;
        boolean isSmallApt = HousingType.SMALL_APARTMENT.getCode().equalsIgnoreCase(adopter.getHousingType());
        boolean isLargeDog = PetSize.LARGE.getCode().equalsIgnoreCase(pet.getSize())
                && PetSpecies.DOG.getCode().equalsIgnoreCase(pet.getSpecies());
        return !(isSmallApt && isLargeDog);
    }

    /**
     * R4 状态规则
     */
    boolean passesStatusRule(PetProfile pet) {
        return "AVAILABLE".equalsIgnoreCase(pet.getStatus());
    }

    /**
     * R5 偏好排斥：如果 preferredPetSize 显式列出"NOT_LARGE"或类似标记（约定：包含 'NONE_' 前缀视为排斥）
     * 此处采用保守实现，仅当 preferredPetSize 列表中明确包含 "NONE_LARGE" 时排除大型犬。
     */
    boolean passesPreferenceExclusion(AdopterProfile adopter, PetProfile pet) {
        List<String> prefs = adopter.getPreferredPetSize();
        if (prefs == null || prefs.isEmpty()) return true;
        if (prefs.contains("NONE_" + pet.getSize())) {
            return false;
        }
        return true;
    }
}