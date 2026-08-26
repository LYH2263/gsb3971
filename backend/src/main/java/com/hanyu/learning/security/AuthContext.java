package com.hanyu.learning.security;

import com.hanyu.learning.common.exception.AppException;
import com.hanyu.learning.common.exception.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthContext {

    private AuthContext() {
    }

    public static AuthUser requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new AppException(HttpStatus.UNAUTHORIZED.value(), ErrorCodes.AUTH_UNAUTHORIZED, "未登录或登录已过期");
        }
        return authUser;
    }
}
