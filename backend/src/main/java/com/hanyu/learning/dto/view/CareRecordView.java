package com.hanyu.learning.dto.view;

import java.time.LocalDateTime;

public record CareRecordView(
    Long id,
    Long customerId,
    String customerName,
    LocalDateTime careDate,
    String content,
    Long performedBy,
    String performerName
) {
}
