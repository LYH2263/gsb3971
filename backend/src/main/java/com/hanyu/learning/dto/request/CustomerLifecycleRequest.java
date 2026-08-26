package com.hanyu.learning.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CustomerLifecycleRequest(
    @NotBlank(message = "动作不能为空")
    String action,
    @NotNull(message = "日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate actionDate,
    Long bedId,
    @Size(max = 255, message = "原因最多255字符")
    String reason
) {
}
