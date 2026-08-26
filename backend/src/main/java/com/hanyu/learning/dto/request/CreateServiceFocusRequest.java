package com.hanyu.learning.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateServiceFocusRequest(
    @NotNull(message = "学员ID不能为空")
    Long customerId,
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称最多100字符")
    String serviceName,
    @NotNull(message = "报名日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate purchaseDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate expireDate,
    @NotBlank(message = "服务状态不能为空")
    @Size(max = 20, message = "服务状态最多20字符")
    String serviceStatus,
    @Size(max = 255, message = "备注最多255字符")
    String note
) {
}

