package com.petadopt.dto;

import lombok.Data;

import java.util.List;

@Data
public class PetUpdateRequest {
    private Long id;
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
    private String status;
}
