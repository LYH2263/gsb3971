package com.hanyu.learning.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    String phone,
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6到32位")
    String password,
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名最多50字符")
    String realName,
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 120, message = "年龄必须小于等于120")
    Integer age,
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别取值非法")
    @Max(value = 2, message = "性别取值非法")
    Integer gender
) {
}
