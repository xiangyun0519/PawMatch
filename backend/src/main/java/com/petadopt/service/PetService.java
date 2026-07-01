package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadopt.common.PageResult;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.exception.ErrorCode;
import com.petadopt.dto.PetCreateRequest;
import com.petadopt.dto.PetQueryRequest;
import com.petadopt.dto.PetUpdateRequest;
import com.petadopt.entity.PetProfile;
import com.petadopt.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetMapper petMapper;

    public PageResult<PetProfile> queryPets(PetQueryRequest request) {
        Page<PetProfile> page = new Page<>(request.getPage(), request.getPageSize());
        
        LambdaQueryWrapper<PetProfile> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(request.getSpecies())) {
            wrapper.eq(PetProfile::getSpecies, request.getSpecies());
        }
        if (StringUtils.hasText(request.getGender())) {
            wrapper.eq(PetProfile::getGender, request.getGender());
        }
        if (StringUtils.hasText(request.getSize())) {
            wrapper.eq(PetProfile::getSize, request.getSize());
        }
        if (request.getMinAge() != null) {
            wrapper.ge(PetProfile::getAgeMonths, request.getMinAge());
        }
        if (request.getMaxAge() != null) {
            wrapper.le(PetProfile::getAgeMonths, request.getMaxAge());
        }
        if (StringUtils.hasText(request.getHealthStatus())) {
            wrapper.eq(PetProfile::getHealthStatus, request.getHealthStatus());
        }
        if (request.getShelterId() != null) {
            wrapper.eq(PetProfile::getShelterId, request.getShelterId());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(PetProfile::getStatus, request.getStatus());
        } else {
            wrapper.eq(PetProfile::getStatus, "AVAILABLE");
        }
        
        wrapper.orderByDesc(PetProfile::getCreatedAt);
        
        IPage<PetProfile> result = petMapper.selectPage(page, wrapper);
        
        return PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    public PetProfile getPetById(Long id) {
        PetProfile pet = petMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "宠物不存在");
        }
        return pet;
    }

    public PetProfile createPet(PetCreateRequest request) {
        PetProfile pet = new PetProfile();
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setAgeMonths(request.getAgeMonths());
        pet.setGender(request.getGender());
        pet.setSize(request.getSize());
        pet.setHealthStatus(request.getHealthStatus());
        pet.setPersonalityTags(request.getPersonalityTags());
        pet.setDescription(request.getDescription());
        pet.setPhotos(request.getPhotos());
        pet.setShelterId(request.getShelterId());
        pet.setStatus("AVAILABLE");
        pet.setCreatedAt(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());
        
        petMapper.insert(pet);
        return pet;
    }

    public PetProfile updatePet(PetUpdateRequest request) {
        PetProfile pet = getPetById(request.getId());
        
        if (StringUtils.hasText(request.getName())) {
            pet.setName(request.getName());
        }
        if (StringUtils.hasText(request.getSpecies())) {
            pet.setSpecies(request.getSpecies());
        }
        if (StringUtils.hasText(request.getBreed())) {
            pet.setBreed(request.getBreed());
        }
        if (request.getAgeMonths() != null) {
            pet.setAgeMonths(request.getAgeMonths());
        }
        if (StringUtils.hasText(request.getGender())) {
            pet.setGender(request.getGender());
        }
        if (StringUtils.hasText(request.getSize())) {
            pet.setSize(request.getSize());
        }
        if (StringUtils.hasText(request.getHealthStatus())) {
            pet.setHealthStatus(request.getHealthStatus());
        }
        if (request.getPersonalityTags() != null) {
            pet.setPersonalityTags(request.getPersonalityTags());
        }
        if (StringUtils.hasText(request.getDescription())) {
            pet.setDescription(request.getDescription());
        }
        if (request.getPhotos() != null) {
            pet.setPhotos(request.getPhotos());
        }
        if (StringUtils.hasText(request.getStatus())) {
            pet.setStatus(request.getStatus());
        }
        
        pet.setUpdatedAt(LocalDateTime.now());
        petMapper.updateById(pet);
        return pet;
    }

    public void deletePet(Long id) {
        PetProfile pet = getPetById(id);
        pet.setStatus("DELETED");
        pet.setUpdatedAt(LocalDateTime.now());
        petMapper.updateById(pet);
    }

    public List<PetProfile> getRecommendedPets(int limit) {
        LambdaQueryWrapper<PetProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetProfile::getStatus, "AVAILABLE")
               .orderByDesc(PetProfile::getCreatedAt)
               .last("LIMIT " + limit);
        return petMapper.selectList(wrapper);
    }

    public List<PetProfile> getPetsByShelterId(Long shelterId) {
        LambdaQueryWrapper<PetProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetProfile::getShelterId, shelterId)
               .orderByDesc(PetProfile::getCreatedAt);
        return petMapper.selectList(wrapper);
    }
}
