package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.dto.StatsResponse;
import com.petadopt.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/shelter/{shelterId}")
    public Result<StatsResponse.ShelterStats> getShelterStats(@PathVariable Long shelterId) {
        StatsResponse.ShelterStats stats = statsService.getShelterStats(shelterId);
        return Result.success(stats);
    }

    @GetMapping("/platform")
    public Result<StatsResponse.PlatformStats> getPlatformStats() {
        StatsResponse.PlatformStats stats = statsService.getPlatformStats();
        return Result.success(stats);
    }

    @GetMapping("/monthly-trend")
    public Result<List<StatsResponse.MonthlyStats>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int months) {
        List<StatsResponse.MonthlyStats> trend = statsService.getMonthlyTrend(months);
        return Result.success(trend);
    }

    @GetMapping("/species-distribution")
    public Result<List<StatsResponse.SpeciesDistribution>> getSpeciesDistribution() {
        List<StatsResponse.SpeciesDistribution> distribution = statsService.getSpeciesDistribution();
        return Result.success(distribution);
    }

    @GetMapping("/full")
    public Result<StatsResponse> getFullStats(
            @RequestParam(required = false) Long shelterId) {
        StatsResponse stats = statsService.getFullStats(shelterId);
        return Result.success(stats);
    }
}
