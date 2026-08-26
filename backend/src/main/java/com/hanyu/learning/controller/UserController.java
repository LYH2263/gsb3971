package com.hanyu.learning.controller;

import com.hanyu.learning.common.api.ApiResponse;
import com.hanyu.learning.dto.request.UpdateUserStatusRequest;
import com.hanyu.learning.dto.view.UserView;
import com.hanyu.learning.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserView>> listUsers() {
        return ApiResponse.success(userService.listUsers());
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserView> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return ApiResponse.success(userService.updateStatus(id, request.status()));
    }
}
