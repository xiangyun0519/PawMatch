package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pet_personality_tag")
public class PetPersonalityTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("pet_id")
    private Long petId;

    private String tag;

    private BigDecimal confidence;
}
