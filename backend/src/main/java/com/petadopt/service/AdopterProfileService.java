package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.exception.ErrorCode;
import com.petadopt.dto.AdopterProfileRequest;
import com.petadopt.entity.AdopterProfile;
import com.petadopt.mapper.AdopterProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdopterProfileService {

    private final AdopterProfileMapper adopterProfileMapper;

    public AdopterProfile getProfileByUserId(Long userId) {
        return adopterProfileMapper.selectOne(
                new LambdaQueryWrapper<AdopterProfile>()
                        .eq(AdopterProfile::getUserId, userId)
        );
    }

    public AdopterProfile createOrUpdateProfile(Long userId, AdopterProfileRequest request) {
        AdopterProfile profile = getProfileByUserId(userId);
        
        if (profile == null) {
            profile = new AdopterProfile();
            profile.setUserId(userId);
            profile.setCreatedAt(LocalDateTime.now());
        }
        
        profile.setHousingType(request.getHousingType());
        profile.setHasChildren(request.getHasChildren());
        profile.setHasElderly(request.getHasElderly());
        profile.setHasOtherPets(request.getHasOtherPets());
        profile.setPetExperience(request.getPetExperience());
        profile.setDailyHoursAvailable(request.getDailyHoursAvailable());
        profile.setPreferredPetSize(request.getPreferredPetSize());
        profile.setPreferredPetAge(request.getPreferredPetAge());
        profile.setAllergyInfo(request.getAllergyInfo());
        profile.setActivityLevel(request.getActivityLevel());
        profile.setAdoptionMotivation(request.getAdoptionMotivation());
        profile.setUpdatedAt(LocalDateTime.now());
        
        if (profile.getId() == null) {
            adopterProfileMapper.insert(profile);
        } else {
            adopterProfileMapper.updateById(profile);
        }
        
        return profile;
    }

    public AdopterProfile getProfileById(Long id) {
        AdopterProfile profile = adopterProfileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "领养人画像不存在");
        }
        return profile;
    }
}
