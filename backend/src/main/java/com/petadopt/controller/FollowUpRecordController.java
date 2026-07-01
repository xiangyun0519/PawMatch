package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.dto.FollowUpRecordRequest;
import com.petadopt.entity.FollowUpRecord;
import com.petadopt.service.FollowUpRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
public class FollowUpRecordController {

    private final FollowUpRecordService followUpRecordService;

    @PostMapping
    public Result<FollowUpRecord> createRecord(@RequestBody FollowUpRecordRequest request) {
        FollowUpRecord record = followUpRecordService.createRecord(request);
        return Result.success(record);
    }

    @GetMapping("/application/{applicationId}")
    public Result<List<FollowUpRecord>> getRecordsByApplication(@PathVariable Long applicationId) {
        List<FollowUpRecord> records = followUpRecordService.getRecordsByApplication(applicationId);
        return Result.success(records);
    }

    @GetMapping("/{id}")
    public Result<FollowUpRecord> getRecord(@PathVariable Long id) {
        FollowUpRecord record = followUpRecordService.getRecordById(id);
        return Result.success(record);
    }

    @PutMapping("/{id}")
    public Result<FollowUpRecord> updateRecord(
            @PathVariable Long id,
            @RequestBody FollowUpRecordRequest request) {
        FollowUpRecord record = followUpRecordService.updateRecord(id, request);
        return Result.success(record);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        followUpRecordService.deleteRecord(id);
        return Result.success(null);
    }

    @GetMapping("/pending")
    public Result<List<FollowUpRecord>> getPendingFollowUps() {
        List<FollowUpRecord> records = followUpRecordService.getPendingFollowUps();
        return Result.success(records);
    }
}
