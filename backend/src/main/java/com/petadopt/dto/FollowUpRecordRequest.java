package com.petadopt.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FollowUpRecordRequest {

    private Long applicationId;

    private Integer daysAfterAdoption;

    private List<String> photos;

    private String petHealthStatus;

    private String petBehaviorStatus;

    private String adopterFeedback;

    private Integer adoptionSatisfaction;

    private String issuesFound;

    private LocalDate nextFollowUpDate;
}
