package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadopt.common.PageResult;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.exception.ErrorCode;
import com.petadopt.dto.ApplicationCreateRequest;
import com.petadopt.dto.ApplicationReviewRequest;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.entity.PetProfile;
import com.petadopt.mapper.AdoptionApplicationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final AdoptionApplicationMapper applicationMapper;
    private final PetService petService;

    public AdoptionApplication createApplication(Long adopterId, ApplicationCreateRequest request) {
        PetProfile pet = petService.getPetById(request.getPetId());
        
        if (!"AVAILABLE".equals(pet.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该宠物暂不可领养");
        }

        AdoptionApplication existingApplication = applicationMapper.selectOne(
                new LambdaQueryWrapper<AdoptionApplication>()
                        .eq(AdoptionApplication::getAdopterId, adopterId)
                        .eq(AdoptionApplication::getPetId, request.getPetId())
                        .in(AdoptionApplication::getStatus, "PENDING", "APPROVED")
        );
        
        if (existingApplication != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "您已提交过该宠物的领养申请");
        }

        AdoptionApplication application = new AdoptionApplication();
        application.setAdopterId(adopterId);
        application.setPetId(request.getPetId());
        application.setApplicantMessage(request.getApplicantMessage());
        application.setStatus("PENDING");
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        
        applicationMapper.insert(application);
        return application;
    }

    public PageResult<AdoptionApplication> getApplicationsByAdopter(Long adopterId, int page, int pageSize) {
        Page<AdoptionApplication> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdoptionApplication::getAdopterId, adopterId)
               .orderByDesc(AdoptionApplication::getCreatedAt);
        
        IPage<AdoptionApplication> result = applicationMapper.selectPage(pageParam, wrapper);
        
        return PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    public PageResult<AdoptionApplication> getApplicationsByShelter(Long shelterId, int page, int pageSize) {
        List<PetProfile> pets = petService.getPetsByShelterId(shelterId);
        List<Long> petIds = pets.stream().map(PetProfile::getId).toList();
        
        if (petIds.isEmpty()) {
            return PageResult.of(List.of(), 0L, page, pageSize);
        }

        Page<AdoptionApplication> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AdoptionApplication::getPetId, petIds)
               .orderByDesc(AdoptionApplication::getCreatedAt);
        
        IPage<AdoptionApplication> result = applicationMapper.selectPage(pageParam, wrapper);
        
        return PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    public AdoptionApplication getApplicationById(Long id) {
        AdoptionApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申请不存在");
        }
        return application;
    }

    public AdoptionApplication reviewApplication(Long id, ApplicationReviewRequest request) {
        AdoptionApplication application = getApplicationById(id);
        
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该申请已处理");
        }

        application.setStatus(request.getStatus());
        application.setShelterReviewNote(request.getShelterReviewNote());
        application.setReviewedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        
        if ("APPROVED".equals(request.getStatus())) {
            PetProfile pet = petService.getPetById(application.getPetId());
            pet.setStatus("RESERVED");
            pet.setUpdatedAt(LocalDateTime.now());
            petService.updatePet(createUpdateRequest(pet));
        }
        
        applicationMapper.updateById(application);
        return application;
    }

    public AdoptionApplication completeApplication(Long id) {
        AdoptionApplication application = getApplicationById(id);
        
        if (!"APPROVED".equals(application.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "申请未通过审核");
        }

        application.setStatus("COMPLETED");
        application.setCompletedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        
        PetProfile pet = petService.getPetById(application.getPetId());
        pet.setStatus("ADOPTED");
        pet.setUpdatedAt(LocalDateTime.now());
        petService.updatePet(createUpdateRequest(pet));
        
        applicationMapper.updateById(application);
        return application;
    }

    public void cancelApplication(Long id, Long adopterId) {
        AdoptionApplication application = getApplicationById(id);
        
        if (!application.getAdopterId().equals(adopterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权取消该申请");
        }
        
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该申请已处理，无法取消");
        }

        application.setStatus("CANCELLED");
        application.setUpdatedAt(LocalDateTime.now());
        applicationMapper.updateById(application);
    }

    private com.petadopt.dto.PetUpdateRequest createUpdateRequest(PetProfile pet) {
        com.petadopt.dto.PetUpdateRequest request = new com.petadopt.dto.PetUpdateRequest();
        request.setId(pet.getId());
        request.setStatus(pet.getStatus());
        return request;
    }
}
