package com.hanyu.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hanyu.learning.common.exception.AppException;
import com.hanyu.learning.common.exception.ErrorCodes;
import com.hanyu.learning.domain.entity.UserEntity;
import com.hanyu.learning.dto.view.UserView;
import com.hanyu.learning.mapper.UserMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PermissionService permissionService;

    public UserService(UserMapper userMapper, PermissionService permissionService) {
        this.userMapper = userMapper;
        this.permissionService = permissionService;
    }

    public List<UserView> listUsers() {
        permissionService.requireAdmin();
        List<UserEntity> entities = userMapper.selectList(
            new LambdaQueryWrapper<UserEntity>().orderByDesc(UserEntity::getId)
        );
        return entities.stream().map(this::toView).toList();
    }

    public UserView updateStatus(Long id, Integer status) {
        permissionService.requireAdmin();
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return toView(user);
    }

    private UserView toView(UserEntity user) {
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
