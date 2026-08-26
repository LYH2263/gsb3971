package com.hanyu.learning.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveRoomRequest(
    Long id,
    @NotNull(message = "楼层不能为空")
    Integer floor,
    @NotBlank(message = "房间号不能为空")
    String roomNo,
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    Integer status
) {
}
