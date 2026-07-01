package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.exception.ErrorCode;
import com.petadopt.dto.FollowUpRecordRequest;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.entity.FollowUpRecord;
import com.petadopt.mapper.AdoptionApplicationMapper;
import com.petadopt.mapper.FollowUpRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowUpRecordService {

    private final FollowUpRecordMapper followUpRecordMapper;
    private final AdoptionApplicationMapper applicationMapper;

    public FollowUpRecord createRecord(FollowUpRecordRequest request) {
        AdoptionApplication application = applicationMapper.selectById(request.getApplicationId());
        if (application == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申请不存在");
        }
        
        if (!"COMPLETED".equals(application.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能对已完成的领养申请创建回访记录");
        }

        FollowUpRecord record = new FollowUpRecord();
        record.setApplicationId(request.getApplicationId());
        record.setDaysAfterAdoption(request.getDaysAfterAdoption());
        record.setPhotos(request.getPhotos());
        record.setPetHealthStatus(request.getPetHealthStatus());
        record.setPetBehaviorStatus(request.getPetBehaviorStatus());
        record.setAdopterFeedback(request.getAdopterFeedback());
        record.setAdoptionSatisfaction(request.getAdoptionSatisfaction());
        record.setIssuesFound(request.getIssuesFound());
        record.setNextFollowUpDate(request.getNextFollowUpDate());
        record.setCreatedAt(LocalDateTime.now());
        
        followUpRecordMapper.insert(record);
        return record;
    }

    public List<FollowUpRecord> getRecordsByApplication(Long applicationId) {
        LambdaQueryWrapper<FollowUpRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpRecord::getApplicationId, applicationId)
               .orderByAsc(FollowUpRecord::getDaysAfterAdoption);
        return followUpRecordMapper.selectList(wrapper);
    }

    public FollowUpRecord getRecordById(Long id) {
        FollowUpRecord record = followUpRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "回访记录不存在");
        }
        return record;
    }

    public FollowUpRecord updateRecord(Long id, FollowUpRecordRequest request) {
        FollowUpRecord record = getRecordById(id);
        
        if (request.getDaysAfterAdoption() != null) {
            record.setDaysAfterAdoption(request.getDaysAfterAdoption());
        }
        if (request.getPhotos() != null) {
            record.setPhotos(request.getPhotos());
        }
        if (request.getPetHealthStatus() != null) {
            record.setPetHealthStatus(request.getPetHealthStatus());
        }
        if (request.getPetBehaviorStatus() != null) {
            record.setPetBehaviorStatus(request.getPetBehaviorStatus());
        }
        if (request.getAdopterFeedback() != null) {
            record.setAdopterFeedback(request.getAdopterFeedback());
        }
        if (request.getAdoptionSatisfaction() != null) {
            record.setAdoptionSatisfaction(request.getAdoptionSatisfaction());
        }
        if (request.getIssuesFound() != null) {
            record.setIssuesFound(request.getIssuesFound());
        }
        if (request.getNextFollowUpDate() != null) {
            record.setNextFollowUpDate(request.getNextFollowUpDate());
        }
        
        followUpRecordMapper.updateById(record);
        return record;
    }

    public void deleteRecord(Long id) {
        FollowUpRecord record = getRecordById(id);
        followUpRecordMapper.deleteById(record.getId());
    }

    public List<FollowUpRecord> getPendingFollowUps() {
        LambdaQueryWrapper<FollowUpRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(FollowUpRecord::getNextFollowUpDate)
               .orderByAsc(FollowUpRecord::getNextFollowUpDate);
        return followUpRecordMapper.selectList(wrapper);
    }
}
