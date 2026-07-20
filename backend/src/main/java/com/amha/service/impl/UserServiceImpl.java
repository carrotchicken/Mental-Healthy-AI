package com.amha.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.amha.common.BusinessException;
import com.amha.config.JwtUtil;
import com.amha.dto.LoginRequest;
import com.amha.dto.LoginResponse;
import com.amha.dto.RegisterRequest;
import com.amha.dto.UserInfoVO;
import com.amha.entity.User;
import com.amha.mapper.UserMapper;
import com.amha.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        // 缓存token，过期时间比JWT略长
        redisTemplate.opsForValue().set("token:" + token, user.getId().toString(),
                24, TimeUnit.HOURS);

        String roleType = user.getUserType() == 2 ? "ADMIN" : "USER";
        UserInfoVO userInfo = buildUserInfoVO(user);

        return LoginResponse.builder()
                .roleType(roleType)
                .token(token)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        BeanUtil.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("token:" + token);
    }

    private UserInfoVO buildUserInfoVO(User user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .gender(user.getGender())
                .genderDisplayName(getGenderName(user.getGender()))
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserType() == 2 ? "管理员" : "普通用户")
                .status(user.getStatus())
                .statusDisplayName(user.getStatus() == 1 ? "正常" : "禁用")
                .displayName(user.getNickname() != null ? user.getNickname() : user.getUsername())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String getGenderName(Integer gender) {
        if (gender == null) return "未知";
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }
}
