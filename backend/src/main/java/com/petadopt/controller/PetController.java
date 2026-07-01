package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.dto.PetCreateRequest;
import com.petadopt.dto.PetQueryRequest;
import com.petadopt.dto.PetUpdateRequest;
import com.petadopt.entity.PetProfile;
import com.petadopt.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping
    public Result<PageResult<PetProfile>> queryPets(PetQueryRequest request) {
        PageResult<PetProfile> result = petService.queryPets(request);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<PetProfile> getPet(@PathVariable Long id) {
        PetProfile pet = petService.getPetById(id);
        return Result.success(pet);
    }

    @PostMapping
    public Result<PetProfile> createPet(@RequestBody PetCreateRequest request) {
        PetProfile pet = petService.createPet(request);
        return Result.success(pet);
    }

    @PutMapping("/{id}")
    public Result<PetProfile> updatePet(@PathVariable Long id, @RequestBody PetUpdateRequest request) {
        request.setId(id);
        PetProfile pet = petService.updatePet(request);
        return Result.success(pet);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return Result.success(null);
    }

    @GetMapping("/recommended")
    public Result<List<PetProfile>> getRecommendedPets(@RequestParam(defaultValue = "8") int limit) {
        List<PetProfile> pets = petService.getRecommendedPets(limit);
        return Result.success(pets);
    }

    @GetMapping("/shelter/{shelterId}")
    public Result<List<PetProfile>> getPetsByShelter(@PathVariable Long shelterId) {
        List<PetProfile> pets = petService.getPetsByShelterId(shelterId);
        return Result.success(pets);
    }
}
