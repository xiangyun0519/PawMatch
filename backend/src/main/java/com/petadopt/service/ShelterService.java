package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.exception.ErrorCode;
import com.petadopt.dto.ShelterCreateRequest;
import com.petadopt.entity.Shelter;
import com.petadopt.mapper.ShelterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShelterService {

    private final ShelterMapper shelterMapper;

    public Shelter createShelter(ShelterCreateRequest request) {
        Shelter existingShelter = shelterMapper.selectOne(
                new LambdaQueryWrapper<Shelter>()
                        .eq(Shelter::getUserId, request.getUserId())
        );
        
        if (existingShelter != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该用户已注册救助站");
        }

        Shelter shelter = new Shelter();
        shelter.setUserId(request.getUserId());
        shelter.setName(request.getName());
        shelter.setType(request.getType());
        shelter.setAddress(request.getAddress());
        shelter.setContactPhone(request.getContactPhone());
        shelter.setLicenseNumber(request.getLicenseNumber());
        shelter.setDescription(request.getDescription());
        shelter.setIsVerified(false);
        shelter.setCreatedAt(LocalDateTime.now());
        shelter.setUpdatedAt(LocalDateTime.now());
        
        shelterMapper.insert(shelter);
        return shelter;
    }

    public Shelter getShelterById(Long id) {
        Shelter shelter = shelterMapper.selectById(id);
        if (shelter == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "救助站不存在");
        }
        return shelter;
    }

    public Shelter getShelterByUserId(Long userId) {
        return shelterMapper.selectOne(
                new LambdaQueryWrapper<Shelter>()
                        .eq(Shelter::getUserId, userId)
        );
    }

    public List<Shelter> getAllVerifiedShelters() {
        return shelterMapper.selectList(
                new LambdaQueryWrapper<Shelter>()
                        .eq(Shelter::getIsVerified, true)
                        .orderByDesc(Shelter::getCreatedAt)
        );
    }

    public Shelter updateShelter(Long id, ShelterCreateRequest request) {
        Shelter shelter = getShelterById(id);
        
        shelter.setName(request.getName());
        shelter.setType(request.getType());
        shelter.setAddress(request.getAddress());
        shelter.setContactPhone(request.getContactPhone());
        shelter.setLicenseNumber(request.getLicenseNumber());
        shelter.setDescription(request.getDescription());
        shelter.setUpdatedAt(LocalDateTime.now());
        
        shelterMapper.updateById(shelter);
        return shelter;
    }

    public void verifyShelter(Long id) {
        Shelter shelter = getShelterById(id);
        shelter.setIsVerified(true);
        shelter.setUpdatedAt(LocalDateTime.now());
        shelterMapper.updateById(shelter);
    }
}
