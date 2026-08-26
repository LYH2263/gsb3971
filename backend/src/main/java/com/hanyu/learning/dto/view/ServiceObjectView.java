package com.hanyu.learning.dto.view;

import java.time.LocalDateTime;

public record ServiceObjectView(
    Long customerId,
    String customerName,
    Long managerUserId,
    String managerName,
    String managerPhone,
    LocalDateTime assignedAt
) {
}

