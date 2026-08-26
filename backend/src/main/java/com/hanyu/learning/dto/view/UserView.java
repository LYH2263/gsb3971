package com.hanyu.learning.dto.view;

public record UserView(
    Long id,
    String phone,
    String realName,
    Integer age,
    Integer gender,
    String role,
    Integer status
) {
}
