package com.hanyu.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveCustomerMealPlanRequest(
    @NotBlank(message = "餐型不能为空")
    String mealType,
    @Size(max = 255, message = "忌口信息最多255字符")
    String dietTaboo,
    @Size(max = 255, message = "备注最多255字符")
    String note
) {
}
