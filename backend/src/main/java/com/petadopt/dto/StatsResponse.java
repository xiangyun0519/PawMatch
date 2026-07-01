package com.petadopt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {

    private ShelterStats shelterStats;
    
    private PlatformStats platformStats;
    
    private List<MonthlyStats> monthlyTrend;
    
    private List<SpeciesDistribution> speciesDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShelterStats {
        private Integer totalPets;
        private Integer availablePets;
        private Integer pendingApplications;
        private Integer completedAdoptions;
        private Integer monthlyViews;
        private Integer monthlyNewPets;
        private Integer monthlyAdoptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformStats {
        private Integer totalUsers;
        private Integer totalAdopters;
        private Integer totalShelters;
        private Integer totalPets;
        private Integer totalApplications;
        private Integer completedAdoptions;
        private Double avgMatchScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStats {
        private String month;
        private Integer adoptions;
        private Integer applications;
        private Integer newPets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpeciesDistribution {
        private String species;
        private Integer count;
        private Double percentage;
    }
}
