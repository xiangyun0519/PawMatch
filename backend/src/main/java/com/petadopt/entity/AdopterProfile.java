package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "adopter_profile", autoResultMap = true)
public class AdopterProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("housing_type")
    private String housingType;

    @TableField("has_children")
    private Boolean hasChildren;

    @TableField("has_elderly")
    private Boolean hasElderly;

    @TableField("has_other_pets")
    private Boolean hasOtherPets;

    @TableField("pet_experience")
    private String petExperience;

    @TableField("daily_hours_available")
    private Integer dailyHoursAvailable;

    @TableField(value = "preferred_pet_size", typeHandler = JacksonTypeHandler.class)
    private List<String> preferredPetSize;

    @TableField(value = "preferred_pet_age", typeHandler = JacksonTypeHandler.class)
    private List<String> preferredPetAge;

    @TableField("allergy_info")
    private String allergyInfo;

    @TableField("activity_level")
    private String activityLevel;

    @TableField("adoption_motivation")
    private String adoptionMotivation;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
