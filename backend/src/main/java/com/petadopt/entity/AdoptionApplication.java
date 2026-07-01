package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("adoption_application")
public class AdoptionApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("adopter_id")
    private Long adopterId;

    @TableField("pet_id")
    private Long petId;

    private String status;

    @TableField("matching_score")
    private BigDecimal matchingScore;

    @TableField("matching_reasons")
    private String matchingReasons;

    @TableField("applicant_message")
    private String applicantMessage;

    @TableField("shelter_review_note")
    private String shelterReviewNote;

    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    @TableField("completed_at")
    private LocalDateTime completedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
