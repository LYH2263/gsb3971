package com.hanyu.learning.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
    @NotBlank(message = "学员姓名不能为空")
    @Size(max = 50, message = "学员姓名最多50字符")
    String name,
    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    String phone,
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 120, message = "年龄必须小于等于120")
    Integer age,
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别取值非法")
    @Max(value = 2, message = "性别取值非法")
    Integer gender,
    @Size(max = 255, message = "备注最多255字符")
    String note
) {
}
