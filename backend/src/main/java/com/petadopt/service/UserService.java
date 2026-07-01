package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.common.exception.BusinessException;
import com.petadopt.common.exception.ErrorCode;
import com.petadopt.dto.LoginRequest;
import com.petadopt.dto.LoginResponse;
import com.petadopt.dto.RegisterRequest;
import com.petadopt.entity.User;
import com.petadopt.mapper.UserMapper;
import com.petadopt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();
    }

    public LoginResponse register(RegisterRequest request) {
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (existingUser != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : "ADOPTER");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .token(token)
                .build();
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    public User getUserByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
    }

    public void updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
