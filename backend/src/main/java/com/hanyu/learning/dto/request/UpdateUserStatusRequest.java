package com.hanyu.learning.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    Integer status
) {
}
