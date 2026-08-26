package com.hanyu.learning.dto.response;

import com.hanyu.learning.dto.view.UserView;

public record LoginResponse(String token, String expireAt, UserView user) {
}
