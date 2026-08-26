package com.hanyu.learning.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateCareRecordRequest(
    @NotNull(message = "学员ID不能为空")
    Long customerId,
    @NotNull(message = "辅导时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime careDate,
    @NotNull(message = "辅导内容不能为空")
    @Size(min = 1, max = 500, message = "辅导内容长度需在1到500字符")
    String content
) {
}
