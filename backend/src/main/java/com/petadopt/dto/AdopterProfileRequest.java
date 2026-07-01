package com.petadopt.dto;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

@Data
public class AdopterProfileRequest {
    private String housingType;
    private Boolean hasChildren;
    private Boolean hasElderly;
    private Boolean hasOtherPets;
    private String petExperience;
    private Integer dailyHoursAvailable;
    private List<String> preferredPetSize;
    private List<String> preferredPetAge;
    private String allergyInfo;
    private String activityLevel;
    private String adoptionMotivation;
}
