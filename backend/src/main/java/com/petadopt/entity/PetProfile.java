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
@TableName(value = "pet_profile", autoResultMap = true)
public class PetProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String species;

    private String breed;

    @TableField("age_months")
    private Integer ageMonths;

    private String gender;

    private String size;

    @TableField("health_status")
    private String healthStatus;

    @TableField(value = "personality_tags", typeHandler = JacksonTypeHandler.class)
    private List<String> personalityTags;

    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> photos;

    @TableField("shelter_id")
    private Long shelterId;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
