package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.dto.ChatRequest;
import com.petadopt.dto.ChatResponse;
import com.petadopt.entity.ChatMessage;
import com.petadopt.entity.ChatSession;
import com.petadopt.service.ChatService;
import com.petadopt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public Result<ChatResponse> chat(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ChatRequest request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        
        ChatResponse response = chatService.chat(userId, request);
        return Result.success(response);
    }

    @GetMapping("/sessions")
    public Result<List<ChatSession>> getSessions(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        
        List<ChatSession> sessions = chatService.getUserSessions(userId);
        return Result.success(sessions);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessage>> getSessionMessages(@PathVariable Long sessionId) {
        List<ChatMessage> messages = chatService.getSessionMessages(sessionId);
        return Result.success(messages);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        
        chatService.deleteSession(sessionId, userId);
        return Result.success(null);
    }
}
