package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.dto.LoginRequest;
import com.petadopt.dto.LoginResponse;
import com.petadopt.dto.RegisterRequest;
import com.petadopt.entity.User;
import com.petadopt.service.UserService;
import com.petadopt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest request) {
        LoginResponse response = userService.register(request);
        return Result.success(response);
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        User user = userService.getUserById(userId);
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        String newToken = jwtUtil.generateToken(userId, username, role);

        LoginResponse response = LoginResponse.builder()
                .id(userId)
                .username(username)
                .role(role)
                .token(newToken)
                .build();

        return Result.success(response);
    }

    @PostMapping("/encrypt-password")
    public Result<Map<String, String>> encryptPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encryptedPassword = encoder.encode(password);
        return Result.success(Map.of("encryptedPassword", encryptedPassword));
    }
}
