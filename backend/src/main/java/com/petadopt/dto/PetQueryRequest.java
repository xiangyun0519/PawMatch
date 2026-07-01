package com.petadopt.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

@Data
public class PetQueryRequest {
    private String species;
    private String gender;
    private String size;
    private Integer minAge;
    private Integer maxAge;
    private String healthStatus;
    private List<String> personalityTags;
    private Long shelterId;
    private String status;
    private Integer page = 1;
    private Integer pageSize = 12;
}
