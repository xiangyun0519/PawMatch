package com.petadopt.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

@Data
public class PetCreateRequest {
    private String name;
    private String species;
    private String breed;
    private Integer ageMonths;
    private String gender;
    private String size;
    private String healthStatus;
    private List<String> personalityTags;
    private String description;
    private List<String> photos;
    private Long shelterId;
}
