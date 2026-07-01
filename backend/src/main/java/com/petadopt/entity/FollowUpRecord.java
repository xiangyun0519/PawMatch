package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("follow_up_record")
public class FollowUpRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("application_id")
    private Long applicationId;

    @TableField("days_after_adoption")
    private Integer daysAfterAdoption;

    private Object photos;

    @TableField("pet_health_status")
    private String petHealthStatus;

    @TableField("pet_behavior_status")
    private String petBehaviorStatus;

    @TableField("adopter_feedback")
    private String adopterFeedback;

    @TableField("adoption_satisfaction")
    private Integer adoptionSatisfaction;

    @TableField("issues_found")
    private String issuesFound;

    @TableField("next_follow_up_date")
    private LocalDate nextFollowUpDate;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
