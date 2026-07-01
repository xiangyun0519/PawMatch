package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_embedding")
public class PetEmbedding {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("pet_id")
    private Long petId;

    private String content;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object metadata;

    private float[] embedding;

    private String model;

    private Integer dimension;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
