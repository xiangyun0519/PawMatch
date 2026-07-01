package com.petadopt.dto;

import lombok.Data;

@Data
public class ShelterCreateRequest {
    private Long userId;
    private String name;
    private String type;
    private String address;
    private String contactPhone;
    private String licenseNumber;
    private String description;
}
