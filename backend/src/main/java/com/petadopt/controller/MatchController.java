package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.entity.MqMessageLog;
import com.petadopt.service.MatchingService;
import com.petadopt.service.MatchingService.MatchResult;
import com.petadopt.service.MqMessageLogService;
import com.petadopt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 匹配接口（异步版）。
 *
 * 流程：
 *  POST /api/match/recommend -> 立即返回 taskId；匹配在 matching_queue 异步执行
 *  GET  /api/match/history/{messageId} -> 查询状态与结果快照
 *  GET  /api/match/explain/{petId} -> 单只宠物的匹配理由（同步，便于详情页）
 */
@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchingService matchingService;
    private final MqMessageLogService mqMessageLogService;
    private final JwtUtil jwtUtil;
    private final com.petadopt.mq.MatchingProducer matchingProducer;
    private final com.petadopt.service.AdopterProfileService adopterProfileService;
    private final com.petadopt.service.PetService petService;

    /**
     * 提交匹配请求，返回 taskId 供前端轮询
     */
    @PostMapping("/recommend")
    public Result<Map<String, Object>> submitRecommend(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "5") int topK) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);

        Map<String, Object> payload = new HashMap<>();
        payload.put("adopterId", userId);
        payload.put("topK", topK);

        String messageId = mqMessageLogService.createPending("MATCH_RECOMMEND", userId, payload);
        matchingProducer.publish(messageId, userId, topK);

        Map<String, Object> resp = new HashMap<>();
        resp.put("taskId", messageId);
        resp.put("status", "PENDING");
        resp.put("pollUrl", "/api/match/history/" + messageId);
        return Result.success(resp);
    }

    /**
     * 轮询任务状态与结果快照
     */
    @GetMapping("/history/{messageId}")
    public Result<Map<String, Object>> getHistory(@PathVariable String messageId) {
        MqMessageLog logRow = mqMessageLogService.findByMessageId(messageId);
        if (logRow == null) {
            return Result.error(404, "任务不存在");
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("taskId", messageId);
        resp.put("status", logRow.getStatus());
        resp.put("retryCount", logRow.getRetryCount());
        resp.put("errorMessage", logRow.getErrorMessage());
        resp.put("result", logRow.getResultSnapshot());
        resp.put("createdAt", logRow.getCreatedAt());
        resp.put("processedAt", logRow.getProcessedAt());
        return Result.success(resp);
    }

    /**
     * 单只宠物匹配解释（同步，供详情页）
     */
    @GetMapping("/explain/{petId}")
    public Result<MatchResult> explain(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long petId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);

        List<MatchResult> results = matchingService.matchPetsForAdopter(userId, 20);
        MatchResult found = results.stream()
                .filter(r -> r.getPetId().equals(petId))
                .findFirst()
                .orElse(null);
        if (found == null) {
            return Result.error(404, "未找到匹配结果");
        }

        var profile = adopterProfileService.getProfileByUserId(userId);
        if (profile != null) {
            String reason = matchingService.generateReason(
                    userId, petId, profile, petService.getPetById(petId),
                    found.getScore() == null ? 0 : found.getScore().doubleValue()
            );
            found.setReasons(reason);
        }
        return Result.success(found);
    }
}