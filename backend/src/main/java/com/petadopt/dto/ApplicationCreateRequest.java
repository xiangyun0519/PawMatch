package com.petadopt.dto;

import lombok.Data;

@Data
public class ApplicationCreateRequest {
    private Long petId;
    private String applicantMessage;
}
