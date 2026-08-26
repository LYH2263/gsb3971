package com.hanyu.learning.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCareLevelRequest(
    @NotBlank(message = "带教档位名称不能为空")
    @Size(max = 50, message = "带教档位名称最多50字符")
    String name,
    @Size(max = 255, message = "描述最多255字符")
    String description,
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    Integer status
) {
}
