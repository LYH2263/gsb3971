package com.hanyu.learning.controller;

import com.hanyu.learning.common.api.ApiResponse;
import com.hanyu.learning.dto.request.LoginRequest;
import com.hanyu.learning.dto.request.RegisterRequest;
import com.hanyu.learning.dto.response.LoginResponse;
import com.hanyu.learning.dto.view.UserView;
import com.hanyu.learning.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<UserView> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}
