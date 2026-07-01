package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.dto.ApplicationCreateRequest;
import com.petadopt.dto.ApplicationReviewRequest;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.service.ApplicationService;
import com.petadopt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public Result<AdoptionApplication> createApplication(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ApplicationCreateRequest request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        AdoptionApplication application = applicationService.createApplication(userId, request);
        return Result.success(application);
    }

    @GetMapping("/my")
    public Result<PageResult<AdoptionApplication>> getMyApplications(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        PageResult<AdoptionApplication> result = applicationService.getApplicationsByAdopter(userId, page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/shelter")
    public Result<PageResult<AdoptionApplication>> getShelterApplications(
            @RequestHeader("Authorization") String authorization,
            @RequestParam Long shelterId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<AdoptionApplication> result = applicationService.getApplicationsByShelter(shelterId, page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<AdoptionApplication> getApplication(@PathVariable Long id) {
        AdoptionApplication application = applicationService.getApplicationById(id);
        return Result.success(application);
    }

    @PostMapping("/{id}/review")
    public Result<AdoptionApplication> reviewApplication(
            @PathVariable Long id,
            @RequestBody ApplicationReviewRequest request) {
        AdoptionApplication application = applicationService.reviewApplication(id, request);
        return Result.success(application);
    }

    @PostMapping("/{id}/complete")
    public Result<AdoptionApplication> completeApplication(@PathVariable Long id) {
        AdoptionApplication application = applicationService.completeApplication(id);
        return Result.success(application);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelApplication(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        applicationService.cancelApplication(id, userId);
        return Result.success(null);
    }
}
