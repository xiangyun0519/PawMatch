package com.petadopt.service;

import com.petadopt.entity.AdopterProfile;
import com.petadopt.entity.PetProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Value("${embedding.model:Qwen/Qwen3-Embedding-8B}")
    private String embeddingModelName;

    /**
     * 维度自适应：从 embeddingModel 动态读取真实维度，禁止硬编码。
     * 首次调用时探测一次，后续复用缓存值。
     */
    private volatile Integer cachedDimension = null;

    public EmbeddingService(@Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 生成 embedding 向量
     */
    public float[] generateEmbedding(String text) {
        return embeddingModel.embed(text);
    }

    /**
     * 探测并返回当前 Embedding 模型的实际维度。
     * 用一个短字符串触发一次 embed 调用，读取结果长度。
     */
    public int detectDimension() {
        if (cachedDimension != null) {
            return cachedDimension;
        }
        float[] probe = embeddingModel.embed("dim-probe");
        cachedDimension = probe.length;
        log.info("Embedding 模型 [{}] 实际维度 = {}", embeddingModelName, cachedDimension);
        return cachedDimension;
    }

    /**
     * 业务用：返回当前维度（带缓存）
     */
    public int getDimension() {
        return cachedDimension != null ? cachedDimension : detectDimension();
    }

    /**
     * 重置维度缓存（切换模型时调用）
     */
    public void resetDimensionCache() {
        cachedDimension = null;
    }

    public String buildPetContent(PetProfile pet) {
        StringBuilder sb = new StringBuilder();

        sb.append("宠物名称：").append(pet.getName()).append("。");
        sb.append("物种：").append(pet.getSpecies()).append("。");

        if (pet.getBreed() != null) {
            sb.append("品种：").append(pet.getBreed()).append("。");
        }

        sb.append("年龄：").append(formatAge(pet.getAgeMonths())).append("。");
        sb.append("性别：").append(pet.getGender()).append("。");
        sb.append("体型：").append(pet.getSize()).append("。");

        if (pet.getHealthStatus() != null) {
            sb.append("健康状况：").append(pet.getHealthStatus()).append("。");
        }

        if (pet.getPersonalityTags() != null && !pet.getPersonalityTags().isEmpty()) {
            sb.append("性格特点：").append(String.join("、", pet.getPersonalityTags())).append("。");
        }

        if (pet.getDescription() != null) {
            sb.append("详细描述：").append(pet.getDescription()).append("。");
        }

        return sb.toString();
    }

    public String buildAdopterContent(AdopterProfile profile) {
        StringBuilder sb = new StringBuilder();

        sb.append("住房类型：").append(profile.getHousingType()).append("。");

        if (Boolean.TRUE.equals(profile.getHasChildren())) {
            sb.append("家中有小孩。");
        }
        if (Boolean.TRUE.equals(profile.getHasElderly())) {
            sb.append("家中有老人。");
        }
        if (Boolean.TRUE.equals(profile.getHasOtherPets())) {
            sb.append("家中已有其他宠物。");
        }

        sb.append("养宠经验：").append(profile.getPetExperience()).append("。");
        sb.append("每天可陪伴时间：").append(profile.getDailyHoursAvailable()).append("小时。");

        if (profile.getPreferredPetSize() != null && !profile.getPreferredPetSize().isEmpty()) {
            sb.append("偏好体型：").append(String.join("、", profile.getPreferredPetSize())).append("。");
        }

        if (profile.getPreferredPetAge() != null && !profile.getPreferredPetAge().isEmpty()) {
            sb.append("偏好年龄：").append(String.join("、", profile.getPreferredPetAge())).append("。");
        }

        if (profile.getAllergyInfo() != null) {
            sb.append("过敏信息：").append(profile.getAllergyInfo()).append("。");
        }

        sb.append("活动水平：").append(profile.getActivityLevel()).append("。");

        if (profile.getAdoptionMotivation() != null) {
            sb.append("领养动机：").append(profile.getAdoptionMotivation()).append("。");
        }

        return sb.toString();
    }

    private String formatAge(int ageMonths) {
        if (ageMonths < 12) {
            return ageMonths + "个月";
        } else {
            int years = ageMonths / 12;
            int months = ageMonths % 12;
            if (months == 0) {
                return years + "岁";
            }
            return years + "岁" + months + "个月";
        }
    }
}