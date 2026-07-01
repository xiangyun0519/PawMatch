package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.dto.ShelterCreateRequest;
import com.petadopt.entity.Shelter;
import com.petadopt.service.ShelterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shelters")
@RequiredArgsConstructor
public class ShelterController {

    private final ShelterService shelterService;

    @PostMapping
    public Result<Shelter> createShelter(@RequestBody ShelterCreateRequest request) {
        Shelter shelter = shelterService.createShelter(request);
        return Result.success(shelter);
    }

    @GetMapping("/{id}")
    public Result<Shelter> getShelter(@PathVariable Long id) {
        Shelter shelter = shelterService.getShelterById(id);
        return Result.success(shelter);
    }

    @GetMapping("/user/{userId}")
    public Result<Shelter> getShelterByUser(@PathVariable Long userId) {
        Shelter shelter = shelterService.getShelterByUserId(userId);
        return Result.success(shelter);
    }

    @GetMapping
    public Result<List<Shelter>> getAllVerifiedShelters() {
        List<Shelter> shelters = shelterService.getAllVerifiedShelters();
        return Result.success(shelters);
    }

    @PutMapping("/{id}")
    public Result<Shelter> updateShelter(@PathVariable Long id, @RequestBody ShelterCreateRequest request) {
        Shelter shelter = shelterService.updateShelter(id, request);
        return Result.success(shelter);
    }

    @PostMapping("/{id}/verify")
    public Result<Void> verifyShelter(@PathVariable Long id) {
        shelterService.verifyShelter(id);
        return Result.success(null);
    }
}
