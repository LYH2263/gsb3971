package com.hanyu.learning.service;

import com.hanyu.learning.common.exception.AppException;
import com.hanyu.learning.common.exception.ErrorCodes;
import com.hanyu.learning.constant.RoleConstants;
import com.hanyu.learning.security.AuthContext;
import com.hanyu.learning.security.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    public AuthUser requireLogin() {
        return AuthContext.requireUser();
    }

    public AuthUser requireAdmin() {
        AuthUser authUser = AuthContext.requireUser();
        if (!RoleConstants.ADMIN.equals(authUser.role())) {
            throw new AppException(HttpStatus.FORBIDDEN.value(), ErrorCodes.FORBIDDEN, "仅管理员可执行该操作");
        }
        return authUser;
    }
}
