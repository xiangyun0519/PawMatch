package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.dto.AdopterProfileRequest;
import com.petadopt.entity.AdopterProfile;
import com.petadopt.service.AdopterProfileService;
import com.petadopt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adopters")
@RequiredArgsConstructor
public class AdopterProfileController {

    private final AdopterProfileService adopterProfileService;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    public Result<AdopterProfile> getMyProfile(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        AdopterProfile profile = adopterProfileService.getProfileByUserId(userId);
        return Result.success(profile);
    }

    @PutMapping("/profile")
    public Result<AdopterProfile> updateProfile(
            @RequestHeader("Authorization") String authorization,
            @RequestBody AdopterProfileRequest request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        AdopterProfile profile = adopterProfileService.createOrUpdateProfile(userId, request);
        return Result.success(profile);
    }

    @GetMapping("/{id}")
    public Result<AdopterProfile> getProfile(@PathVariable Long id) {
        AdopterProfile profile = adopterProfileService.getProfileById(id);
        return Result.success(profile);
    }
}
