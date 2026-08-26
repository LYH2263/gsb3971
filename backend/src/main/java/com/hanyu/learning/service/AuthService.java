package com.hanyu.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hanyu.learning.common.exception.AppException;
import com.hanyu.learning.common.exception.ErrorCodes;
import com.hanyu.learning.constant.RoleConstants;
import com.hanyu.learning.domain.entity.UserEntity;
import com.hanyu.learning.dto.request.LoginRequest;
import com.hanyu.learning.dto.request.RegisterRequest;
import com.hanyu.learning.dto.response.LoginResponse;
import com.hanyu.learning.dto.view.UserView;
import com.hanyu.learning.mapper.UserMapper;
import com.hanyu.learning.security.AuthUser;
import com.hanyu.learning.security.JwtTokenProvider;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss"
    );

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public UserView register(RegisterRequest request) {
        long exists = userMapper.selectCount(
            new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getPhone, request.phone())
        );
        if (exists > 0) {
            throw new AppException(HttpStatus.CONFLICT.value(), ErrorCodes.PHONE_ALREADY_USED, "手机号已被注册");
        }

        UserEntity entity = new UserEntity();
        entity.setPhone(request.phone());
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        entity.setRealName(request.realName());
        entity.setAge(request.age());
        entity.setGender(request.gender());
        entity.setRole(RoleConstants.STAFF);
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        userMapper.insert(entity);
        return toUserView(entity);
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userMapper.selectOne(
            new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getPhone, request.phone()).last("limit 1")
        );
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException(
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCodes.AUTH_INVALID_CREDENTIALS,
                "账号或密码错误"
            );
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new AppException(HttpStatus.FORBIDDEN.value(), ErrorCodes.FORBIDDEN, "账号已停用");
        }

        AuthUser authUser = new AuthUser(user.getId(), user.getPhone(), user.getRole(), user.getRealName());
        String token = jwtTokenProvider.generateToken(authUser);
        String expireAt = LocalDateTime.ofInstant(jwtTokenProvider.getExpireAt(), ZoneId.systemDefault())
            .format(DEFAULT_DATE_TIME_FORMATTER);
        return new LoginResponse(token, expireAt, toUserView(user));
    }

    private UserView toUserView(UserEntity user) {
        return new UserView(
            user.getId(),
            user.getPhone(),
            user.getRealName(),
            user.getAge(),
            user.getGender(),
            user.getRole(),
            user.getStatus()
        );
    }
}
