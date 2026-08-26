package com.hanyu.learning.dto.view;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ServiceFocusView(
    Long id,
    Long customerId,
    String customerName,
    String serviceName,
    LocalDate purchaseDate,
    LocalDate expireDate,
    String serviceStatus,
    String note,
    Long createdBy,
    String createdByName,
    LocalDateTime createdAt
) {
}

