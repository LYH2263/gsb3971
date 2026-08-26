package com.hanyu.learning.dto.view;

import java.time.LocalDate;

public record CustomerMealPlanView(
    Long id,
    Long customerId,
    String customerName,
    LocalDate weekStartDate,
    String mealType,
    String dietTaboo,
    String note,
    Long createdBy
) {
}
